package com.sze.findmeamechanic.managers;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryBounds;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sze.findmeamechanic.models.Client;
import com.sze.findmeamechanic.models.FinishedJob;
import com.sze.findmeamechanic.models.Job;
import com.sze.findmeamechanic.models.Repairman;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FirestoreManager {

    private FirebaseAuth fAuth;
    private FirebaseUser loggedInUser;
    private FirebaseFirestore fStore;
    private FirebaseStorage fStorage;
    private StorageReference storageRef;
    private StorageReference pictureRef;
    private DocumentReference docRef;
    private CollectionReference collectionRef;
    private String userID, docID;

    public FirestoreManager() {
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        fStorage = FirebaseStorage.getInstance();
        storageRef = fStorage.getReference();
        userID = fAuth.getUid();
        loggedInUser = fAuth.getCurrentUser();
    }

    //region --------------------- CLIENT FUNCTIONS ---------------------
    public DocumentReference getClientName() {
        docRef = fStore.collection("Clients").document(userID);
        return docRef;
    }

    public Query getClientActiveJobs() {
        return fStore.collection("ActiveJobs").whereEqualTo("jobSenderID", userID).orderBy("jobDate", Query.Direction.DESCENDING);
    }

    public Query getClientFinishedJobs() {
        return fStore.collection("FinishedJobs").whereEqualTo("jobSenderID", userID).orderBy("jobDate", Query.Direction.DESCENDING);
    }

    public void getApplicants(String docID, final GetListCallback callback) {
        final List<DocumentSnapshot> matchingDocs = new ArrayList<>();
        fStore.collection("ActiveJobs").document(docID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull final Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.get("jobApplicantsId") != null) {
                        final ArrayList<String> list = (ArrayList<String>) doc.get("jobApplicantsId");
                        Task queryTask = null;
                        for (int i = 0; i < list.size(); i++) {
                            queryTask = fStore.collection("Repairmen").whereEqualTo("repID", list.get(i)).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (DocumentSnapshot doc : task.getResult()) {
                                            matchingDocs.add(doc);
                                        }
                                    }
                                }
                            });
                        }
                        //wait for query inside for loop to finish, then pass matchingDocs with a callback
                        Tasks.whenAllComplete(queryTask).addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
                            @Override
                            public void onComplete(@NonNull Task<List<Task<?>>> task) {
                                callback.onListCallback(matchingDocs);
                            }
                        });
                    }
                }
            }
        });
    }

    public ListenerRegistration notifyAboutNewApplicant(final GetFieldCallback callback) {
        ListenerRegistration listener = fStore.collection("ActiveJobs").whereEqualTo("jobSenderID", userID).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    return;
                }
                for (DocumentChange documentChange : value.getDocumentChanges()) {
                    DocumentSnapshot documentSnapshot = documentChange.getDocument();
                    String id = documentSnapshot.getString("jobName");
                    switch (documentChange.getType()) {
                        case MODIFIED:
                            callback.onTaskResultCallback(id);
                            break;
                    }
                }
            }
        });

        return listener;
    }

    /*public void getActiveJobCoordinates(String docID, final GetSnapshotCallback callback) {
        docRef = fStore.collection("ActiveJobs").document(docID);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    callback.onGetFieldCallback(documentSnapshot);
                }
            }
        });
    }

    public void getFinishedJobCoordinates(String docID, final GetSnapshotCallback callback) {
        docRef = fStore.collection("FinishedJobs").document(docID);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    callback.onGetFieldCallback(documentSnapshot);
                }
            }
        });
    } */

    public void saveJob(String name, String type, String description, String deadline, String timestamp, String pathToImage,
                        ArrayList<Double> inputLocationArr, Map<String, Object> geoHashLocation, String locationText) {
        docRef = fStore.collection("ActiveJobs").document();
        docID = docRef.getId();

        Job jobObj = new Job(userID, docID, name, type, description, deadline, timestamp, pathToImage, inputLocationArr, locationText);
        docRef.set(jobObj);
        docRef.update(geoHashLocation);
    }

    public void deleteJob(final String docID) {
        //delete job applicants sub collection
        CollectionReference subCollection = fStore.collection("ActiveJobs").document(docID).collection("jobApplicants");
        subCollection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        document.getReference().delete();
                    }
                }
            }
        });

        //delete picture from storage
        docRef = fStore.collection("ActiveJobs").document(docID);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    if (!documentSnapshot.getString("jobPictureUrl").isEmpty()) {
                        String pictureUrl = documentSnapshot.getString("jobPictureUrl");
                        storageRef = fStorage.getReferenceFromUrl(pictureUrl);
                        storageRef.delete();
                    }
                }
            }
        });
        //delete parent collection
        docRef.delete();
    }

    public void updateJobData(String docID, String type, String description, String pathToImage, String deadline) {
        fStore.collection("ActiveJobs").document(docID).update("jobType", type);
        fStore.collection("ActiveJobs").document(docID).update("jobDescription", description);
        fStore.collection("ActiveJobs").document(docID).update("jobPictureUrl", pathToImage);
        fStore.collection("ActiveJobs").document(docID).update("jobDeadline", deadline);
    }

    public void getImageUrl(String docID, final GetSnapshotCallback callback) {
        docRef = fStore.collection("ActiveJobs").document(docID);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    callback.onGetFieldCallback(documentSnapshot);
                }
            }
        });
    }

    public void getRepairmanDetails(String docID, final GetSnapshotCallback callback) {
        docRef = fStore.collection("Repairmen").document(docID);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (documentSnapshot != null)
                    callback.onGetFieldCallback(documentSnapshot);
            }
        });
    }

    public void getFinalRepairmanDetails(final String docID, final GetSnapshotCallback callback) {
        fStore.collection("ActiveJobs").document(docID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    String repID = doc.getString("finalApplicant");
                    getRepairmanDetails(repID, callback);
                }
            }
        });
    }

    public void getFinalRepairmanDetailsOfFinishedJob(final String docID, final GetSnapshotCallback callback) {
        fStore.collection("FinishedJobs").document(docID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    String repID = doc.getString("jobApplicantID");
                    getRepairmanDetails(repID, callback);
                }
            }
        });
    }

    public void getRepairmanJobsRatings(String repID, final GetRatingCallback callback) {
        fStore.collection("FinishedJobs").whereEqualTo("jobApplicantID", repID).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                List<Integer> ratings = new ArrayList<>();
                if (task.isSuccessful()) {
                    for (DocumentSnapshot doc : task.getResult()) {
                        ratings.add(doc.getLong("rating").intValue());
                    }
                    callback.onRatingCallback(ratings);
                }
            }
        });
    }

    public void selectRepairmanForJob(String repID, final String docID, final GetQueryCallback callback) {
        fStore.collection("ActiveJobs").document(docID).update("finalApplicant", repID).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                fStore.collection("ActiveJobs").document(docID).update("notifyRepman", true).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        callback.onQueryCallback();
                    }
                });
            }
        });
        //delete array of applicants
        deleteApplicants(docID);
    }

    public void deleteApplicants(String docID) {
        fStore.collection("ActiveJobs").document(docID).update("jobApplicantsId", FieldValue.delete());
    }

    public void checkForFinalApplicant(String docID, final GetQueryCallback callback) {
        fStore.collection("ActiveJobs").document(docID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult().getString("finalApplicant") != null) {
                    callback.onQueryCallback();
                }
            }
        });
    }

    public void checkIfJobFinished(String docID, final GetQueryCallback callback) {
        fStore.collection("FinishedJobs").document(docID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult().getString("jobApplicantID") != null) {
                    callback.onQueryCallback();
                }
            }
        });
    }

    public void rate(String docID, float rating) {
        fStore.collection("FinishedJobs").document(docID).update("rating", FieldValue.increment(rating));
    }

    public void getClientDetails(String docID, final GetSnapshotCallback callback) {
        docRef = fStore.collection("Clients").document(docID);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (documentSnapshot != null)
                    callback.onGetFieldCallback(documentSnapshot);
            }
        });
    }

    public void modifyClientData(final String name, final String phoneNr, final String email, final String newPictureUrl, final GetQueryCallback callback) {

        //if user uploaded new picture, delete the old one
        if (!newPictureUrl.isEmpty()) {
            docRef = fStore.collection("Clients").document(fAuth.getUid());
            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        if (!documentSnapshot.getString("pathToImage").isEmpty()) {
                            final String pictureUrl = documentSnapshot.getString("pathToImage");
                            storageRef = fStorage.getReferenceFromUrl(pictureUrl);
                            //if delete is completen THEN save the new picture url
                            storageRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    fStore.collection("Clients").document(fAuth.getUid()).update("clientName", name);
                                    fStore.collection("Clients").document(fAuth.getUid()).update("clientPhoneNr", phoneNr);
                                    fStore.collection("Clients").document(fAuth.getUid()).update("clientEmail", email);
                                    fStore.collection("Clients").document(fAuth.getUid()).update("pathToImage", newPictureUrl);
                                    callback.onQueryCallback();
                                }
                            });
                        }
                    }
                }
            });
        } else {
            fStore.collection("Clients").document(fAuth.getUid()).update("clientName", name);
            fStore.collection("Clients").document(fAuth.getUid()).update("clientPhoneNr", phoneNr);
            fStore.collection("Clients").document(fAuth.getUid()).update("clientEmail", email);
            callback.onQueryCallback();
        }
    }

    // --------------------------------------------------------
    //endregion

    //region -------------------- REPAIRMAN FUNCTIONS --------------------
    public void queryLocationHashes(double userLat, double userLng, int inputRadius, final GetListCallback callback) {
        collectionRef = fStore.collection("ActiveJobs");

        //searching within the distance set by user
        final GeoLocation userLocation = new GeoLocation(userLat, userLng);
        final double searchRadius = inputRadius * 1000;

        List<GeoQueryBounds> bounds = GeoFireUtils.getGeoHashQueryBounds(userLocation, searchRadius);
        final List<Task<QuerySnapshot>> tasks = new ArrayList<>();
        for (GeoQueryBounds b : bounds) {
            Query q = collectionRef
                    .orderBy("geohash")
                    .startAt(b.startHash)
                    .endAt(b.endHash);
            tasks.add(q.get());
        }

        //collect all results together into a list
        Tasks.whenAllComplete(tasks).addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
            @Override
            public void onComplete(@NonNull Task<List<Task<?>>> t) {
                List<DocumentSnapshot> matchingDocs = new ArrayList<>();

                for (Task<QuerySnapshot> task : tasks) {
                    QuerySnapshot snap = task.getResult();
                    for (final DocumentSnapshot doc : snap.getDocuments()) {
                        ArrayList<Double> coordinates = (ArrayList<Double>) doc.get("jobLocationCoordinates");
                        double lat = coordinates.get(0);
                        double lng = coordinates.get(1);
                        GeoLocation docLocation = new GeoLocation(lat, lng);
                        double distance = GeoFireUtils.getDistanceBetween(docLocation, userLocation);
                        if (distance <= searchRadius) {
                            matchingDocs.add(doc);
                        }
                    }
                }
                callback.onListCallback(matchingDocs);
            }
        });
    }

    public void applyToJob(final String docID, final GetQueryCallback callback) {
        collectionRef = fStore.collection("ActiveJobs").document(docID).collection("jobApplicants");
        docRef = fStore.collection("Repairmen").document(userID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot doc = task.getResult();
                if (doc.exists()) {
                    String name = doc.getString("repName");
                    Map<String, Object> data = new HashMap<>();
                    data.put("repName", name);
                    data.put("repId", userID);
                    collectionRef.add(data);
                    fStore.collection("ActiveJobs").document(docID).update("applicants", data);
                    fStore.collection("ActiveJobs").document(docID).update("jobApplicantsId", FieldValue.arrayUnion(userID));
                    callback.onQueryCallback();
                }
            }
        });
    }

    public void finishJob(String jobSenderID, final String docID, String jobName, String jobType, String jobDescription, String jobDeadline, String jobDate,
                          String jobPictureUrl, String locationText, String repmanID, String finishDate, String jobSheetUrl) {
        docRef = fStore.collection("FinishedJobs").document(docID);
        FinishedJob jobObj = new FinishedJob(jobSenderID, docID, jobName, jobType, jobDescription, jobDeadline, jobDate, jobPictureUrl, repmanID, finishDate, locationText, jobSheetUrl);
        docRef.set(jobObj).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                //delete job from ActiveJobs collection
                fStore.collection("ActiveJobs").document(docID).delete();
            }
        });
    }

    public void checkIfApplied(String docID, final GetQueryCallback callback) {
        collectionRef = fStore.collection("ActiveJobs").document(docID).collection("jobApplicants");
        collectionRef.whereEqualTo("repId", userID).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    callback.onQueryCallback();
                }
            }
        });
    }

    public Query getRepairmanActiveJobs() {
        //  return fStore.collection("ActiveJobs").whereArrayContains("jobApplicantsId", userID);
        return fStore.collection("ActiveJobs").whereEqualTo("finalApplicant", userID);
    }

    public void getJobSenderDetails(String docID, final GetSnapshotCallback callback) {
        fStore.collection("ActiveJobs").document(docID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                fStore.collection("Clients").whereEqualTo("clientID", documentSnapshot.getString("jobSenderID")).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            // callback.onTaskResultCallback(doc.getString("clientName"));
                            callback.onGetFieldCallback(doc);
                        }
                    }
                });
            }
        });
    }

    public void uploadJobSheet(Context context, String fileName, final UploadUrlCallback callback) {
        Uri file = Uri.fromFile(new File(context.getFilesDir() + "/" + fileName));
        final StorageReference jobSheetRef = storageRef.child("job_sheets/" + file.getLastPathSegment());
        UploadTask uploadTask = jobSheetRef.putFile(file);

        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                return jobSheetRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    callback.onUploadSuccessCallback(downloadUri.toString());
                } else {
                    callback.onUploadFailedCallback();
                }
            }
        });
    }

    public Query getRepairmanFinishedJobs() {
        return fStore.collection("FinishedJobs").whereEqualTo("jobApplicantID", userID).orderBy("jobDate", Query.Direction.DESCENDING);
    }

    public void modifyRepairmanData(final String name, final String phoneNr, final String professionInput, final String email,
                                    final String companyNameInput, final String companyAddressInput, final String taxNumberInput,
                                    final String newPictureUrl, final GetQueryCallback callback) {

        //if user uploaded new picture, delete the old one
        if (!newPictureUrl.isEmpty()) {
            docRef = fStore.collection("Repairmen").document(fAuth.getUid());
            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        if (!documentSnapshot.getString("pathToImage").isEmpty()) {
                            final String pictureUrl = documentSnapshot.getString("pathToImage");
                            storageRef = fStorage.getReferenceFromUrl(pictureUrl);
                            //if delete is completen THEN save the new picture url
                            storageRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    fStore.collection("Repairmen").document(fAuth.getUid()).update("repName", name);
                                    fStore.collection("Repairmen").document(fAuth.getUid()).update("repPhoneNr", phoneNr);
                                    fStore.collection("Repairmen").document(fAuth.getUid()).update("repEmail", email);
                                    fStore.collection("Repairmen").document(fAuth.getUid()).update("repProfession", professionInput);
                                    fStore.collection("Repairmen").document(fAuth.getUid()).update("repCompanyName", companyNameInput);
                                    fStore.collection("Repairmen").document(fAuth.getUid()).update("repCompanyAddress", companyAddressInput);
                                    fStore.collection("Repairmen").document(fAuth.getUid()).update("taxNumber", taxNumberInput);
                                    fStore.collection("Repairmen").document(fAuth.getUid()).update("pathToImage", newPictureUrl);
                                    callback.onQueryCallback();
                                }
                            });
                        }
                    }
                }
            });
        } else {
            fStore.collection("Repairmen").document(fAuth.getUid()).update("repName", name);
            fStore.collection("Repairmen").document(fAuth.getUid()).update("repPhoneNr", phoneNr);
            fStore.collection("Repairmen").document(fAuth.getUid()).update("repEmail", email);
            fStore.collection("Repairmen").document(fAuth.getUid()).update("repProfession", professionInput);
            fStore.collection("Repairmen").document(fAuth.getUid()).update("repCompanyName", companyNameInput);
            fStore.collection("Repairmen").document(fAuth.getUid()).update("repCompanyAddress", companyAddressInput);
            fStore.collection("Repairmen").document(fAuth.getUid()).update("taxNumber", taxNumberInput);
            callback.onQueryCallback();
        }
    }

    public void updateCredentials(final FirebaseUser user, String oldPassword, final String newPassword, final String newEmail, final GetQueryCallback callback) {
        final String oldEmail = user.getEmail();
        //update pass
        if (!oldPassword.isEmpty()) {
            AuthCredential credentials = EmailAuthProvider.getCredential(oldEmail, oldPassword);
            user.reauthenticate(credentials).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        user.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    callback.onQueryCallback();
                                }
                            }
                        });
                    }
                }
            });
        }

        //update email
        if (!newEmail.isEmpty()) {
            AuthCredential credentials = EmailAuthProvider.getCredential(oldEmail, oldPassword);
            user.reauthenticate(credentials).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        user.updateEmail(newEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    callback.onQueryCallback();
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    public ListenerRegistration notifyApplicant(String repUserID, final GetFieldCallback callback) {
        ListenerRegistration listener = fStore.collection("ActiveJobs").whereEqualTo("finalApplicant", repUserID).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    return;
                }
                for (DocumentChange documentChange : value.getDocumentChanges()) {
                    DocumentSnapshot documentSnapshot = documentChange.getDocument();
                    String id = documentSnapshot.getString("jobName");
                    switch (documentChange.getType()) {
                        case MODIFIED:
                            callback.onTaskResultCallback(id);
                            break;
                    }
                }
            }
        });

        return listener;
    }

    //TODO
    // valamit csinálni
    public ListenerRegistration notifyAboutMessages(final GetFieldCallback callback) {
        ListenerRegistration listener = fStore.collection("ActiveJobs").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable final QuerySnapshot snapshotValue, @Nullable FirebaseFirestoreException error) {
                for (final DocumentChange documentChange : snapshotValue.getDocumentChanges()) {
                    documentChange.getDocument().getReference().collection("Chats").whereNotEqualTo("messageUserId", fAuth.getUid()).addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            for (DocumentChange documentChange2 : value.getDocumentChanges()) {
                                DocumentSnapshot documentSnapshot = documentChange2.getDocument();
                                //String id = documentSnapshot.getString("senderID");
                                String asd = documentChange.getDocument().getString("jobName");
                                switch (documentChange2.getType()) {
                                    case ADDED:
                                        callback.onTaskResultCallback(asd);
                                        break;
                                }
                            }
                        }
                    });
                }
            }
        });

        return listener;
    }

    //endregion

    //region -------------------- REGISTRATION --------------------
    public void saveRepairman(String fullName, String email, String phoneNumber, String pathToImage) {
        String newUserId = fAuth.getCurrentUser().getUid();
        docRef = fStore.collection("Repairmen").document(newUserId);
        Repairman repmanObj = new Repairman(newUserId, fullName, email, phoneNumber, "", "", "", "", pathToImage);
        docRef.set(repmanObj);
    }

    public void saveClient(String fullName, String email, String phoneNumber, String pathToImage) {
        String newUserId = fAuth.getCurrentUser().getUid();
        docRef = fStore.collection("Clients").document(fAuth.getUid());
        Client clientObj = new Client(newUserId, fullName, email, phoneNumber, pathToImage);
        docRef.set(clientObj);
    }

    public void getProfessionList(final GetFieldCallback callback) {
        collectionRef = fStore.collection("ProfessionList");

        collectionRef.orderBy("Name").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                        String professionName = document.getString("Name");
                        callback.onTaskResultCallback(professionName);
                    }
                    callback.onSuccessfulQueryCallback();
                }
            }
        });
    }

    public void updateRepairmanData(String professionInput, String companyNameInput, String companyAddressInput, String taxNumberInput) {
        fStore.collection("Repairmen").document(fAuth.getUid()).update("repProfession", professionInput);
        fStore.collection("Repairmen").document(fAuth.getUid()).update("repCompanyName", companyNameInput);
        fStore.collection("Repairmen").document(fAuth.getUid()).update("repCompanyAddress", companyAddressInput);
        fStore.collection("Repairmen").document(fAuth.getUid()).update("taxNumber", taxNumberInput);
    }
    // ------------------------------------------------------------------
    //endregion

    //region ------------------------- SHARED FUNCTIONS -------------------------
    public void resetPassword(String email, final Context context) {
        if (Patterns.EMAIL_ADDRESS.matcher(email).matches() && email.length() > 0) {
            fAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, "Jelszó helyreállítás elküldve! Kérlek, ellenőrizd a postafiókod!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Hiba történt! Kérlek, ellenőrizd a megadott e-mail cím helyességét!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    public void getActiveJobDetails(String docID, final GetSnapshotCallback callback) {
        docRef = fStore.collection("ActiveJobs").document(docID);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (documentSnapshot != null)
                    callback.onGetFieldCallback(documentSnapshot);
            }
        });
    }

    public void getFinishedJobDetails(String docID, final GetSnapshotCallback callback) {
        docRef = fStore.collection("FinishedJobs").document(docID);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (documentSnapshot != null)
                    callback.onGetFieldCallback(documentSnapshot);
            }
        });
    }

    public void addProfession(final String newProfession) {
        collectionRef = fStore.collection("ProfessionList");
        Query query = collectionRef.whereEqualTo("Name", newProfession);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().isEmpty()) {
                        Map<String, Object> docData = new HashMap<>();
                        docData.put("Name", newProfession);
                        collectionRef.document(Objects.requireNonNull(fAuth.getUid())).set(docData);
                    }
                }
            }
        });
    }

    public StorageReference getUserPictureRef(String imageId) {
        pictureRef = storageRef.child("profile_images/" + imageId);
        return pictureRef;
    }

    public StorageReference getJobPictureRef(String imageId) {
        pictureRef = storageRef.child("job_images/" + imageId);
        return pictureRef;
    }

    public Query getMessages(String docID) {
        return fStore.collection("ActiveJobs").document(docID).collection("Chats").orderBy("messageTime");
    }

    public void sendMessages(String docID, String message) {
        Date dateTime = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yy/MM/dd-HH:mm:ss");
        String messageTime = dateFormat.format(dateTime);
        Map<String, Object> data = new HashMap<>();
        data.put("messageText", message);
        data.put("messageUserId", userID);
        data.put("messageTime", messageTime);
        data.put("messageUser", fAuth.getCurrentUser().getDisplayName());
        // data.put("senderID", "asdfsdf213123");
        fStore.collection("ActiveJobs").document(docID).collection("Chats").add(data);
    }

    public void deleteUser(final FirebaseUser user, final String oldPassword, final GetQueryCallback callback) {
        user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    callback.onQueryCallback();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                AuthCredential credentials = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);
                user.reauthenticate(credentials).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        deleteUser(user, oldPassword, callback);
                    }
                });
            }
        });
    }
    // ------------------------------------------------------------------
    //endregion

    //------------------------- GETTERS & SETTERS -------------------------
    public FirebaseUser getLoggedInUser() {
        return loggedInUser;
    }

    public void setLoggedInUser(FirebaseUser loggedInUser) {
        this.loggedInUser = loggedInUser;
    }

    public FirebaseAuth getFAuth() {
        return fAuth;
    }

    public void setFAuth(FirebaseAuth fAuth) {
        this.fAuth = fAuth;
    }

    public FirebaseFirestore getFStore() {
        return fStore;
    }

    public void setFStore(FirebaseFirestore fStore) {
        this.fStore = fStore;
    }

    public DocumentReference getDocRef() {
        return docRef;
    }

    public void setDocRef(DocumentReference docRef) {
        this.docRef = docRef;
    }

    public CollectionReference getCollectionRef() {
        return collectionRef;
    }

    public void setCollectionRef(CollectionReference collectionRef) {
        this.collectionRef = collectionRef;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public FirebaseStorage getfStorage() {
        return fStorage;
    }

    public void setfStorage(FirebaseStorage fStorage) {
        this.fStorage = fStorage;
    }

    public StorageReference getStorageRef() {
        return storageRef;
    }

    public void setStorageRef(StorageReference storageRef) {
        this.storageRef = storageRef;
    }

    //------------------------------------------------------------------------------
    //endregion

    //region ------------------------- CALLBACK INTERFACES -------------------------
    public interface GetFieldCallback {
        void onTaskResultCallback(String str);

        void onSuccessfulQueryCallback();
    }

    public interface GetSnapshotCallback {
        void onGetFieldCallback(DocumentSnapshot documentSnapshot);
    }

    public interface GetListCallback {
        void onListCallback(List<DocumentSnapshot> list);
    }

    public interface GetRatingCallback {
        void onRatingCallback(List<Integer> list);
    }

    public interface GetQueryCallback {
        void onQueryCallback();
    }

    public interface UploadUrlCallback {
        void onUploadSuccessCallback(String imageUrl);

        void onUploadFailedCallback();
    }
    //-------------------------------------------------------------------------------
    //endregion

}
