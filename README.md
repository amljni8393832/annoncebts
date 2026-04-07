# 📱 BTS App – Application Mobile

[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Android](https://img.shields.io/badge/Platform-Android-green.svg)]()

---

## 🔹 Description

**BTS App** est une application mobile destinée aux étudiants et enseignants de BTS.  
Elle permet de **créer, gérer et publier des annonces** pour les cours, examens, soutenances, stages et emplois du temps.  

Les utilisateurs autorisés (Administrateur ou Enseignant avec permissions) peuvent :  
- Créer des annonces avec titre, description, catégorie, date et audience cible  
- Ajouter des fichiers joints (PDF, image)  
- Enregistrer en **brouillon** ou marquer **prête à publier**  
- Consulter et modifier leurs annonces  

L'application assure :  
- **Validation en temps réel** pour éviter les erreurs  
- **Sauvegarde automatique** pour éviter la perte de données  
- **Traçabilité et contrôle d'accès** selon les rôles des utilisateurs  

---

## 🌟 Fonctionnalités

- [x] Création d'annonce (titre, description, catégorie, date)  
- [x] Gestion des fichiers joints (PDF, JPEG, PNG, DOCX)  
- [x] Sauvegarde en brouillon ou publication immédiate  
- [x] Interface conviviale pour Android  
- [x] Contrôle des droits d'accès pour Admin et Enseignants  

---

## 🏗 Architecture

L'application suit le modèle **BCE + DAO** :  

- **Boundary (UI)** : `AnnonceCreationActivity` – formulaire et interactions utilisateur  
- **Control** : `AnnonceCreationViewModel` & `AnnonceCreationService` – logique métier et validation  
- **DAO** : `AnnonceDAO` & `UtilisateurDAO` – accès aux données  
- **Entity** : `Annonce`, `Utilisateur`, `Fichier` – structures de données  
