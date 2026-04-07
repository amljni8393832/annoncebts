package com.example.annoncebts.models;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;
import java.util.Date;
import java.util.concurrent.Executors;

@Database(entities = {Notification.class, Utilisateur.class, Filiere.class, Niveau.class}, version = 11, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract NotificationDao notificationDao();
    public abstract UtilisateurDao utilisateurDao();
    public abstract FiliereDao filiereDao();
    public abstract NiveauDao niveauDao();

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "annonce_bts_database")
                            .fallbackToDestructiveMigration()
                            .addCallback(new Callback() {
                                @Override
                                public void onOpen(@NonNull SupportSQLiteDatabase db) {
                                    super.onOpen(db);
                                    seedDatabase(INSTANCE);
                                }
                            })
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static void seedDatabase(AppDatabase db) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                UtilisateurDao dao = db.utilisateurDao();
                
                // Check if admin exists
                Utilisateur admin = dao.getUserByEmail("admin@gmail.com");
                if (admin == null) {
                    admin = new Utilisateur();
                    admin.setNom("Administrateur Système");
                    admin.setEmail("admin@gmail.com");
                    admin.setMotDePasse("admin1234");
                    admin.setRole(Role.ADMINISTRATEUR);
                    admin.setDateCreation(new Date());
                    dao.insert(admin);
                }

                // Populate Filieres
                FiliereDao fDao = db.filiereDao();
                if (fDao.getAllFilieres().isEmpty()) {
                    String[] filieres = {"CPI", "DAI", "EII", "ELT", "PME", "MI"};
                    for (String fName : filieres) {
                        Filiere f = new Filiere();
                        f.setNomFiliere(fName);
                        fDao.insert(f);
                    }
                }

                // Populate Niveaux
                NiveauDao nDao = db.niveauDao();
                if (nDao.getAllNiveaux().isEmpty()) {
                    String[] niveaux = {"1ère Année", "2ème Année"};
                    for (String nLibelle : niveaux) {
                        Niveau n = new Niveau();
                        n.setLibelle(nLibelle);
                        nDao.insert(n);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
