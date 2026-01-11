package com.example.sprintproject.model;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class FirestoreModel {

    private static final AtomicReference<FirestoreModel> INSTANCE = new AtomicReference<>();

    private final FirebaseFirestore firestoreDB;

    private final MutableLiveData<List<Expense>> expensesData = new MutableLiveData<>();
    private final MutableLiveData<List<Budget>> budgetsData = new MutableLiveData<>();

    private final MutableLiveData<List<SavingsCircleModel>> memberCircles =
            new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<List<SavingsCircleModel>> ownerCircles =
            new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<List<SavingsCircleModel>> incomingInvites =
            new MutableLiveData<>(new ArrayList<>());

    private final ExecutorService ioExec = Executors.newSingleThreadExecutor();

    private final Map<String, MutableLiveData<Double>> circleTotalLiveDataMap = new HashMap<>();

    private ListenerRegistration invitesReg;
    private ListenerRegistration cgReg;
    private ListenerRegistration ownerReg;
    private ListenerRegistration expensesReg;
    private ListenerRegistration budgetsReg;

    private static final String EXPENSES = "expenses";
    private static final String USERS = "users";
    private static final String TAG = "Firestore";
    private static final String CATEGORY_FIELD = "category";
    private static final String BUDGETS = "budgets";
    private static final String SAVING_CIRCLES = "savingsCircles";
    private static final String PENDING_INVITES = "pendingInvites";
    private static final String MEMBER_EMAILS = "memberEmails";
    private static final String MEMBER_UIDS = "memberUids";

    private FirestoreModel() {
        firestoreDB = FirebaseFirestore.getInstance();
    }

    public static FirestoreModel getInstance() {
        FirestoreModel result = INSTANCE.get();
        if (result == null) {
            FirestoreModel newInstance = new FirestoreModel();
            if (INSTANCE.compareAndSet(null, newInstance)) {
                result = newInstance;
            } else {
                result = INSTANCE.get();
            }
        }
        return result;
    }

    public Task<DocumentReference> addExpense(Expense newExpense) {
        String uid = FirebaseModel.getInstance().getUID();
        return firestoreDB.collection(USERS).document(uid)
                .collection(EXPENSES).add(newExpense);
    }

    public MutableLiveData<List<Expense>> getExpenses() {
        if (expensesReg != null) {
            expensesReg.remove();
            expensesReg = null;
        }

        expensesData.setValue(new ArrayList<>());

        String uid = FirebaseModel.getInstance().getUID();

        expensesReg = firestoreDB.collection(USERS)
                .document(uid)
                .collection(EXPENSES)
                .orderBy("date", Query.Direction.DESCENDING)
                .addSnapshotListener((snap, e) -> {
                    if (e != null) {
                        Log.w(TAG, "getExpenses listener error", e);
                        return;
                    }
                    if (snap == null) {
                        return;
                    }

                    ioExec.execute(() -> {
                        List<Expense> out = new ArrayList<>(snap.size());
                        for (DocumentSnapshot d : snap.getDocuments()) {
                            Expense curr = d.toObject(Expense.class);
                            if (curr != null) {
                                out.add(curr);
                            }
                        }
                        if (!sameExpenseList(expensesData.getValue(), out)) {
                            expensesData.postValue(out);
                        }
                    });
                });

        return expensesData;
    }

    public void getExpensesForSavingsCircle(String uid, String groupGoalId, Date startDate,
                                            Date endDate, OnMemberExpenseListener listener) {
        firestoreDB.collection(USERS)
                .document(uid)
                .collection(EXPENSES)
                .whereEqualTo("groupGoalId", groupGoalId)
                .addSnapshotListener((snap, e) -> {
                    double total = 0.0;
                    if (snap != null && !snap.isEmpty()) {
                        for (DocumentSnapshot d : snap.getDocuments()) {
                            Expense curr = d.toObject(Expense.class);
                            if (curr != null && curr.getDate() != null
                                    && !curr.getDate().before(startDate)
                                    && !curr.getDate().after(endDate)) {
                                total += curr.getAmount();
                            }
                        }
                    }
                    listener.onExpenseRetrieved(uid, total);
                });
    }

    private static boolean sameExpenseList(List<Expense> a, List<Expense> b) {
        if (a == null || b == null) {
            return false;
        }
        if (a.size() != b.size()) {
            return false;
        }
        for (int i = 0; i < a.size(); i++) {
            if (!expensesEqual(a.get(i), b.get(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean expensesEqual(Expense ea, Expense eb) {
        if (ea == null || eb == null) {
            return false;
        }
        return Objects.equals(ea.getName(), eb.getName())
                && Double.compare(ea.getAmount(), eb.getAmount()) == 0
                && Objects.equals(ea.getDate(), eb.getDate())
                && Objects.equals(ea.getCategory(), eb.getCategory())
                && Objects.equals(ea.getGroupGoalId(), eb.getGroupGoalId());
    }

    public MutableLiveData<Double> getCategoryTotalExpense(String category, Date start, Date end) {
        MutableLiveData<Double> totalLiveData = new MutableLiveData<>(0.0);
        String uid = FirebaseModel.getInstance().getUID();

        firestoreDB.collection(USERS).document(uid)
                .collection(EXPENSES)
                .whereEqualTo(CATEGORY_FIELD, category)
                .get()
                .addOnSuccessListener(qs -> ioExec.execute(() -> {
                    double total = 0.0;
                    for (DocumentSnapshot d : qs.getDocuments()) {
                        Expense e = d.toObject(Expense.class);
                        if (e != null && e.getDate() != null
                                && !e.getDate().before(start) && !e.getDate().after(end)) {
                            total += e.getAmount();
                        }
                    }
                    totalLiveData.postValue(total);
                }))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "getCategoryTotalExpense(client filter) failed", e);
                    totalLiveData.setValue(0.0);
                });
        return totalLiveData;
    }

    public Task<DocumentReference> addBudget(Budget newBudget) {
        String uid = FirebaseModel.getInstance().getUID();
        return firestoreDB.collection(USERS).document(uid)
                .collection(BUDGETS)
                .add(newBudget)
                .addOnSuccessListener(ref -> newBudget.setId(ref.getId()));
    }

    public MutableLiveData<List<Budget>> getBudgets() {
        String uid = FirebaseModel.getInstance().getUID();

        if (budgetsReg != null) {
            budgetsReg.remove();
            budgetsReg = null;
        }

        budgetsReg = firestoreDB.collection(USERS)
                .document(uid)
                .collection(BUDGETS)
                .orderBy("date", Query.Direction.DESCENDING)
                .addSnapshotListener((snap, e) -> {
                    if (e != null) {
                        Log.e(TAG, "getBudgets listener error", e);
                        budgetsData.postValue(new ArrayList<>());
                        return;
                    }
                    if (snap == null) {
                        return;
                    }

                    ioExec.execute(() -> {
                        List<Budget> out = new ArrayList<>(snap.size());
                        for (DocumentSnapshot d : snap.getDocuments()) {
                            Budget curr = d.toObject(Budget.class);
                            if (curr != null) {
                                curr.setId(d.getId());
                                out.add(curr);
                            }
                        }
                        if (!sameBudgetList(budgetsData.getValue(), out)) {
                            budgetsData.postValue(out);
                        }
                    });
                });
        return budgetsData;
    }

    private static boolean sameBudgetList(List<Budget> a, List<Budget> b) {
        if (a == null || b == null) {
            return false;
        }
        if (a.size() != b.size()) {
            return false;
        }
        for (int i = 0; i < a.size(); i++) {
            String ai = a.get(i) != null ? a.get(i).getId() : null;
            String bi = b.get(i) != null ? b.get(i).getId() : null;
            if (!Objects.equals(ai, bi)) {
                return false;
            }
        }
        return true;
    }




    public Task<Boolean> categoryExistsForUser(String category) {
        String uid = FirebaseModel.getInstance().getUID();
        return firestoreDB.collection(USERS)
                .document(uid)
                .collection(BUDGETS)
                .whereEqualTo(CATEGORY_FIELD, category)
                .limit(1)
                .get()
                .continueWith(task ->
                        task.isSuccessful()
                                && task.getResult() != null
                                && !task.getResult().isEmpty());
    }



    public MutableLiveData<Budget> getBudgetByCategory(String category) {
        MutableLiveData<Budget> budgetLiveData = new MutableLiveData<>();
        String uid = FirebaseModel.getInstance().getUID();

        firestoreDB.collection(USERS)
                .document(uid)
                .collection(BUDGETS)
                .whereEqualTo(CATEGORY_FIELD, category)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Budget budget = querySnapshot.getDocuments().get(0).toObject(Budget.class);
                        budgetLiveData.setValue(budget);
                    } else {
                        budgetLiveData.setValue(null);
                    }
                })
                .addOnFailureListener(e ->
                        budgetLiveData.setValue(null));

        return budgetLiveData;
    }

    public Task<Void> updateBudgetAmountByCategory(String category, double newAmount) {
        String uid = FirebaseModel.getInstance().getUID();
        return firestoreDB.collection(USERS).document(uid)
                .collection(BUDGETS)
                .whereEqualTo(CATEGORY_FIELD, category)
                .limit(1)
                .get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful() || task.getResult() == null
                            || task.getResult().isEmpty()) {
                        throw new Exception("No budget found for category: " + category);
                    }
                    return task.getResult().getDocuments().get(0).getReference()
                            .update("amount", newAmount);
                });
    }

    public Task<Budget> getBudgetByCategoryOnce(String category) {
        String uid = FirebaseModel.getInstance().getUID();
        return firestoreDB.collection(USERS)
                .document(uid)
                .collection(BUDGETS)
                .whereEqualTo(CATEGORY_FIELD, category)
                .limit(1)
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful() || task.getResult() == null
                            || task.getResult().isEmpty()) {
                        throw new Exception("No budget found for category: " + category);
                    }
                    DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                    return task.getResult().getDocuments().get(0).toObject(Budget.class);
                });
    }

    public Task<Void> deleteExpensesInRangeClientFiltered(String category, Date start, Date end) {
        String uid = FirebaseModel.getInstance().getUID();
        return firestoreDB.collection(USERS).document(uid)
                .collection(EXPENSES)
                .whereEqualTo(CATEGORY_FIELD, category)
                .get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        throw new Exception("Failed to fetch expenses for deletion");
                    }
                    WriteBatch batch = firestoreDB.batch();
                    int count = 0;
                    for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                        Expense e = doc.toObject(Expense.class);
                        if (e != null && e.getDate() != null
                                && !e.getDate().before(start) && !e.getDate().after(end)) {
                            batch.delete(doc.getReference());
                            if (++count % 450 == 0) {
                                batch.commit().getResult();
                                batch = firestoreDB.batch();
                            }
                        }
                    }
                    return batch.commit();
                });
    }

    public Task<DocumentReference> addCycleAdjustmentExpense(String category,
                                                             double spentAmount, Date when) {
        Expense adj = FinancialFactory.createExpense("Calculated",
                spentAmount, when, category, null, null);
        return addExpense(adj);
    }

    public Task<DocumentReference> addSavingsCircle(SavingsCircleModel newSC) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        newSC.setCreatorUid(uid); // optional but useful

        return firestoreDB.collection(USERS).document(uid)
                .collection(SAVING_CIRCLES)
                .add(newSC)
                .onSuccessTask(ref -> {
                    // persist the ref id into a field so we can query by it later
                    String gid = ref.getId();
                    newSC.setId(gid);
                    newSC.setId(gid);
                    Map<String, Object> updateData = new HashMap<>();
                    updateData.put("globalId", gid);
                    return ref.set(updateData, SetOptions.merge()).continueWith(t -> ref);
                });
    }


    public MutableLiveData<List<SavingsCircleModel>> getSavingsCirclesByOwner() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            ownerCircles.setValue(new ArrayList<>());
            return ownerCircles;
        }
        String uid = auth.getCurrentUser().getUid();

        if (ownerReg != null) {
            ownerReg.remove();
        }

        ownerReg = firestoreDB.collection(USERS).document(uid)
                .collection(SAVING_CIRCLES)
                .addSnapshotListener((snap, e) -> {
                    if (e != null) {
                        Log.e("SC", "Owner listener error", e);
                        ownerCircles.postValue(new ArrayList<>());
                        return;
                    }
                    List<SavingsCircleModel> out = new ArrayList<>();
                    if (snap != null) {
                        for (DocumentSnapshot d : snap.getDocuments()) {
                            SavingsCircleModel curr = d.toObject(SavingsCircleModel.class);
                            if (curr != null) {
                                curr.setId(d.getId());
                                out.add(curr);
                            }
                        }
                    }
                    ownerCircles.postValue(out);
                });

        return ownerCircles;
    }

    public Task<Void> sendCircleInvite(String creatorUid, String circleId, String inviteEmail) {
        String email = inviteEmail.toLowerCase();
        return FirebaseFirestore.getInstance()
                .collection(USERS).document(creatorUid)
                .collection(SAVING_CIRCLES).document(circleId)
                .update(PENDING_INVITES, FieldValue.arrayUnion(email));
    }

    public Task<Void> acceptCircleInvite(String creatorUid, String circleId, String myEmail) {
        String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String myEmailLower = myEmail.toLowerCase();

        DocumentReference circleRef = FirebaseFirestore.getInstance()
                .collection(USERS).document(creatorUid)
                .collection(SAVING_CIRCLES).document(circleId);

        return FirebaseFirestore.getInstance().runTransaction(tx -> {
            DocumentSnapshot snap = tx.get(circleRef);
            if (!snap.exists()) {
                throw new RuntimeException("Circle not found");
            }

            CircleInviteData data = extractCircleInviteData(snap);
            processAcceptedInvite(data, myEmailLower, myUid);
            updateCircleInviteTransaction(tx, circleRef, data);
            return null;
        });
    }

    private CircleInviteData extractCircleInviteData(DocumentSnapshot snap) {
        List<String> pending = (List<String>) snap.get(PENDING_INVITES);
        List<String> members = (List<String>) snap.get(MEMBER_EMAILS);
        List<String> memberUids = (List<String>) snap.get(MEMBER_UIDS);
        Map<String, Date> joinAt = (Map<String, Date>) snap.get("memberJoinAt");

        return new CircleInviteData(
                pending != null ? pending : new ArrayList<>(),
                members != null ? members : new ArrayList<>(),
                memberUids != null ? memberUids : new ArrayList<>(),
                joinAt != null ? joinAt : new java.util.HashMap<>()
        );
    }

    private void processAcceptedInvite(CircleInviteData data, String myEmailLower, String myUid) {
        boolean accepted = data.pending.remove(myEmailLower);
        if (!accepted) {
            return;
        }

        if (!data.members.contains(myEmailLower)) {
            data.members.add(myEmailLower);
        }
        if (!data.memberUids.contains(myUid)) {
            data.memberUids.add(myUid);
        }
        if (!data.joinAt.containsKey(myEmailLower)) {
            data.joinAt.put(myEmailLower, DateModel.getCurrentDate().getValue());
        }
    }

    private void updateCircleInviteTransaction(
            com.google.firebase.firestore.Transaction tx,
            DocumentReference circleRef,
            CircleInviteData data) {
        tx.update(circleRef, PENDING_INVITES, data.pending);
        tx.update(circleRef, MEMBER_EMAILS, data.members);
        tx.update(circleRef, MEMBER_UIDS, data.memberUids);
        tx.update(circleRef, "memberJoinAt", data.joinAt);
    }

    public MutableLiveData<List<SavingsCircleModel>> getSavingsCirclesForCurrentUser() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            memberCircles.setValue(new ArrayList<>());
            return memberCircles;
        }
        String email = auth.getCurrentUser().getEmail();
        if (email == null) {
            memberCircles.setValue(new ArrayList<>());
            return memberCircles;
        }
        String qEmail = email.trim().toLowerCase(java.util.Locale.US);

        if (cgReg != null) {
            cgReg.remove();
        }

        cgReg = FirebaseFirestore.getInstance()
                .collectionGroup(SAVING_CIRCLES)
                .whereArrayContains(MEMBER_EMAILS, qEmail)
                .addSnapshotListener((snap, e) -> {
                    if (e != null) {
                        Log.e("SC", "CG listener error", e);
                        memberCircles.postValue(new ArrayList<>());
                        return;
                    }
                    List<SavingsCircleModel> out = new ArrayList<>();
                    if (snap != null) {
                        for (DocumentSnapshot d : snap.getDocuments()) {
                            SavingsCircleModel sc = d.toObject(SavingsCircleModel.class);
                            if (sc != null) {
                                sc.setId(d.getId());
                            }
                            out.add(sc);
                        }
                    }
                    memberCircles.postValue(out);
                });

        return memberCircles;
    }

    private Task<String> lookupUidByEmail(String emailLower) {
        return FirebaseFirestore.getInstance()
                .collection(USERS)
                .whereEqualTo("emailLower", emailLower)
                .limit(1)
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful() || task.getResult() == null
                            || task.getResult().isEmpty()) {
                        return null;
                    }
                    return task.getResult().getDocuments().get(0).getId();
                });
    }

    private Task<List<String>> resolveMemberUids(DocumentSnapshot circleSnap) {
        List<String> memberUids = (List<String>) circleSnap.get(MEMBER_UIDS);
        if (memberUids != null && !memberUids.isEmpty()) {
            return Tasks.forResult(new ArrayList<>(memberUids));
        }

        List<String> emails = (List<String>) circleSnap.get(MEMBER_EMAILS);
        if (emails == null || emails.isEmpty()) {
            return Tasks.forResult(new ArrayList<>());
        }
        List<Task<String>> lookups = new ArrayList<>();
        for (String e : emails) {
            if (e != null) {
                lookups.add(lookupUidByEmail(e.trim().toLowerCase(java.util.Locale.US)));
            }
        }
        return Tasks.whenAllSuccess(lookups)
                .continueWith(t -> {
                    List<?> results = t.getResult();
                    ArrayList<String> out = new ArrayList<>();
                    for (Object o : results) {
                        if (o instanceof String) {
                            out.add((String) o);
                        }
                    }
                    return out;
                });
    }

    private Task<Void> batchDeleteQuery(com.google.firebase.firestore.Query q) {
        return q.get().continueWithTask(task -> {
            if (!task.isSuccessful() || task.getResult() == null) {
                return Tasks.forResult(null);
            }
            WriteBatch batch = firestoreDB.batch();
            int count = 0;
            for (DocumentSnapshot d : task.getResult().getDocuments()) {
                batch.delete(d.getReference());
                if (++count % 450 == 0) {
                    // commit mid-way to avoid 500 op limit
                    batch.commit().getResult();
                    batch = firestoreDB.batch();
                }
            }
            return batch.commit();
        });
    }

    private Task<Void> deleteBudgetsByCircle(String uid, String circleId) {
        com.google.firebase.firestore.Query q = FirebaseFirestore.getInstance()
                .collection(USERS).document(uid)
                .collection(BUDGETS)
                .whereEqualTo("circleId", circleId);
        return batchDeleteQuery(q);
    }

    private Task<Void> deleteExpensesByCircle(String uid, String circleId,
                                              String categoryForCircle) {
        com.google.firebase.firestore.Query q1 = FirebaseFirestore.getInstance()
                .collection(USERS).document(uid)
                .collection(EXPENSES)
                .whereEqualTo("groupGoalId", circleId);

        com.google.firebase.firestore.Query q2 = FirebaseFirestore.getInstance()
                .collection(USERS).document(uid)
                .collection(EXPENSES)
                .whereEqualTo(CATEGORY_FIELD, categoryForCircle);

        Task<Void> t1 = batchDeleteQuery(q1);
        Task<Void> t2 = batchDeleteQuery(q2);
        return Tasks.whenAll(t1, t2);
    }

    public Task<Void> deleteSavingsCircleCascade(String creatorUid, String circleId) {
        DocumentReference circleRef = FirebaseFirestore.getInstance()
                .collection(USERS).document(creatorUid)
                .collection(SAVING_CIRCLES).document(circleId);

        return circleRef.get().continueWithTask(t -> {
            DocumentSnapshot snap = validateAndGetCircleSnapshot(t);
            String categoryForCircle = buildCategoryName(snap, circleId);
            return resolveMemberUids(snap).continueWithTask(uidsTask ->
                    executeCascadeDeletion(creatorUid, circleId, categoryForCircle,
                            uidsTask.getResult(), circleRef));
        });
    }

    private DocumentSnapshot validateAndGetCircleSnapshot(Task<DocumentSnapshot> task)
            throws Exception {
        if (!task.isSuccessful() || task.getResult() == null || !task.getResult().exists()) {
            throw new Exception("Circle not found");
        }
        return task.getResult();
    }

    private String buildCategoryName(DocumentSnapshot snap, String circleId) {
        String groupName = snap.getString("groupName");
        return (groupName == null) ? ("Circle: " + circleId) : ("Circle: " + groupName);
    }

    private List<String> ensureCreatorInMemberUids(List<String> memberUids, String creatorUid) {
        List<String> result = (memberUids != null)
                ? new ArrayList<>(memberUids) : new ArrayList<>();
        if (!result.contains(creatorUid)) {
            result.add(creatorUid);
        }
        return result;
    }

    private Task<Void> executeCascadeDeletion(String creatorUid, String circleId,
                                              String categoryForCircle,
                                              List<String> memberUids,
                                              DocumentReference circleRef) {
        List<String> allMemberUids = ensureCreatorInMemberUids(memberUids, creatorUid);
        List<Task<Void>> deletionTasks = buildDeletionTasks(allMemberUids,
                circleId, categoryForCircle);
        deletionTasks.add(circleRef.delete());
        return Tasks.whenAll(deletionTasks);
    }

    private List<Task<Void>> buildDeletionTasks(List<String> memberUids, String circleId,
                                                String categoryForCircle) {
        List<Task<Void>> tasks = new ArrayList<>();
        for (String uid : memberUids) {
            if (uid == null || uid.isEmpty()) {
                continue;
            }
            tasks.add(deleteBudgetsByCircle(uid, circleId));
            tasks.add(deleteExpensesByCircle(uid, circleId, categoryForCircle));
        }
        return tasks;
    }

    public MutableLiveData<List<SavingsCircleModel>> getIncomingInvitesForCurrentUser() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null || auth.getCurrentUser().getEmail() == null) {
            incomingInvites.setValue(new ArrayList<>());
            return incomingInvites;
        }
        String my = auth.getCurrentUser().getEmail().trim().toLowerCase(java.util.Locale.US);

        if (invitesReg != null) {
            invitesReg.remove();
        }
        invitesReg = FirebaseFirestore.getInstance()
                .collectionGroup(SAVING_CIRCLES)
                .whereArrayContains(PENDING_INVITES, my)
                .addSnapshotListener((snap, e) -> {
                    if (e != null) {
                        Log.e("SC", "Invites listener error", e);
                        return; // keep whatever is shown
                    }
                    List<SavingsCircleModel> out = new ArrayList<>();
                    if (snap != null) {
                        for (DocumentSnapshot d : snap.getDocuments()) {
                            SavingsCircleModel sc = d.toObject(SavingsCircleModel.class);
                            if (sc != null) {
                                sc.setId(d.getId());
                            }
                            out.add(sc);
                        }
                    }
                    incomingInvites.postValue(out);
                });
        return incomingInvites;
    }

    public Task<Void> declineCircleInvite(String creatorUid, String circleId, String myEmail) {
        String me = myEmail.toLowerCase();
        return FirebaseFirestore.getInstance()
                .collection(USERS).document(creatorUid)
                .collection(SAVING_CIRCLES).document(circleId)
                .update(PENDING_INVITES, com.google.firebase.firestore.FieldValue.arrayRemove(me));
    }
    public MutableLiveData<List<SavingsCircleModel>> getUserGroupGoals() {
        androidx.lifecycle.MediatorLiveData<List<SavingsCircleModel>> out
                = new androidx.lifecycle.MediatorLiveData<>();
        out.setValue(new ArrayList<>());

        MutableLiveData<List<SavingsCircleModel>> owners  = getSavingsCirclesByOwner();
        MutableLiveData<List<SavingsCircleModel>> members = getSavingsCirclesForCurrentUser();

        java.util.concurrent.atomic.AtomicReference<List<SavingsCircleModel>> last
                = new java.util.concurrent.atomic.AtomicReference<>(new ArrayList<>());

        java.util.function.Consumer<Void> recompute = ignore -> {
            List<SavingsCircleModel> ownersList = getSafeListValue(owners);
            List<SavingsCircleModel> membersList = getSafeListValue(members);
            List<SavingsCircleModel> merged = mergeAndDeduplicateCircles(ownersList, membersList);
            updateIfChanged(last, merged, out);
        };

        out.addSource(owners,  x -> recompute.accept(null));
        out.addSource(members, x -> recompute.accept(null));

        return out;
    }

    private static List<SavingsCircleModel> getSafeListValue(
            MutableLiveData<List<SavingsCircleModel>> liveData) {
        List<SavingsCircleModel> value = liveData.getValue();
        return value != null ? value : new ArrayList<>();
    }

    private static List<SavingsCircleModel> mergeAndDeduplicateCircles(
            List<SavingsCircleModel> owners, List<SavingsCircleModel> members) {
        java.util.LinkedHashMap<String, SavingsCircleModel> map = new java.util.LinkedHashMap<>();
        addCirclesToMap(map, owners);
        addCirclesToMap(map, members);
        return new ArrayList<>(map.values());
    }

    private static void addCirclesToMap(
            java.util.LinkedHashMap<String, SavingsCircleModel> map,
            List<SavingsCircleModel> circles) {
        for (SavingsCircleModel sc : circles) {
            if (sc != null && sc.getId() != null) {
                map.put(sc.getId(), sc);
            }
        }
    }

    private static void updateIfChanged(
            java.util.concurrent.atomic.AtomicReference<List<SavingsCircleModel>> last,
            List<SavingsCircleModel> merged,
            androidx.lifecycle.MediatorLiveData<List<SavingsCircleModel>> out) {
        List<SavingsCircleModel> prev = last.get();
        if (!sameIds(prev, merged)) {
            last.set(merged);
            out.setValue(merged);
        }
    }

    private static boolean sameIds(List<SavingsCircleModel> a, List<SavingsCircleModel> b) {
        if (a.size() != b.size()) {
            return false;
        }
        for (int i = 0; i < a.size(); i++) {
            String ai = a.get(i) != null ? a.get(i).getId() : null;
            String bi = b.get(i) != null ? b.get(i).getId() : null;
            if (!java.util.Objects.equals(ai, bi)) {
                return false;
            }
        }
        return true;
    }

    private String getCurrentUid() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            throw new IllegalStateException("User must be logged in to access Firestore data.");
        }
        return user.getUid();
    }

    public Task<Budget> getBudgetByCircleIdOnce(String circleId) {
        return budgetsCollection()
                .whereEqualTo("circleId", circleId)
                .limit(1)
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful() || task.getResult() == null
                            || task.getResult().isEmpty()) {
                        return null;
                    }
                    DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                    Budget b = doc.toObject(Budget.class);
                    if (b != null) {
                        b.setId(doc.getId());
                    }
                    return b;
                });
    }

    public Task<Void> addOrUpdateBudget(Budget b) {
        if (b.getId() != null && !b.getId().isEmpty()) {
            return budgetsCollection().document(b.getId()).set(b, SetOptions.merge());
        } else {
            return budgetsCollection().add(b)
                    .onSuccessTask(ref -> {
                        b.setId(ref.getId()); // keep local in sync
                        return Tasks.forResult(null);
                    });
        }
    }


    public Task<Void> upsertBudgetForCircle(String circleId, Budget payload) {
        return getBudgetByCircleIdOnce(circleId)
                .onSuccessTask(existing -> {
                    if (existing != null) {
                        existing.setAmount(payload.getAmount());
                        existing.setFrequency(payload.getFrequency());
                        existing.setCategory(payload.getCategory());

                        return addOrUpdateBudget(existing);
                    } else {
                        payload.setCircleId(circleId);
                        return addOrUpdateBudget(payload);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG,
                        "upsertBudgetForCircle failed", e));

    }

    private CollectionReference budgetsCollection() {
        return FirebaseFirestore.getInstance()
                .collection(USERS)
                .document(getCurrentUid())
                .collection(BUDGETS);
    }

    public LiveData<Double> getCircleTotalLiveData(String creatorUid, String circleId) {
        String key = creatorUid + "_" + circleId;
        return circleTotalLiveDataMap.computeIfAbsent(key, k -> new MutableLiveData<>(0.0));
    }

    public void trackSavingsCircleTotal(String creatorUid, String circleId) {
        String key = creatorUid + "_" + circleId;
        MutableLiveData<Double> liveData = circleTotalLiveDataMap.computeIfAbsent(key,
                k -> new MutableLiveData<>(0.0));

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection(USERS)
                .document(creatorUid)
                .collection(SAVING_CIRCLES)
                .document(circleId)
                .addSnapshotListener((snapshot, e) -> {
                    if (snapshot != null && snapshot.exists()) {
                        Double total = snapshot.getDouble("currentAmount");
                        if (total != null) {
                            liveData.postValue(total);
                        }
                    }
                });
    }

    public void updateSavingsCircleCurrentAmount(String creatorUid, String circleId,
                                                 double amount) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // For simplicity, find the creatorUid first (or pass it in)
        db.collection(USERS)
                .document(creatorUid)
                .collection(SAVING_CIRCLES)
                .document(circleId)
                .update("currentAmount", FieldValue.increment(amount));
    }

    public Task<DocumentSnapshot> getSavingsCircleById(String creatorUid, String circleId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection(USERS)
                .document(creatorUid)
                .collection(SAVING_CIRCLES)
                .document(circleId)
                .get();
    }

    public void updateUserProfilePicture(String pictureID) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("profilePicture", pictureID);

            FirebaseFirestore.getInstance()
                    .collection(USERS)
                    .document(user.getUid())
                    .set(data, SetOptions.merge())  // <- update a field
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Profile picture updated"))
                    .addOnFailureListener(e -> Log.e(TAG,
                            "Failed to update profile picture", e));
        }
    }

    private static class CircleInviteData {
        private final List<String> pending;
        private final List<String> members;
        private final List<String> memberUids;
        private final Map<String, Date> joinAt;

        CircleInviteData(List<String> pending, List<String> members,
                         List<String> memberUids, Map<String, Date> joinAt) {
            this.pending = pending;
            this.members = members;
            this.memberUids = memberUids;
            this.joinAt = joinAt;
        }
    }

    public interface OnMemberExpenseListener {
        void onExpenseRetrieved(String uid, double total);
    }
}