# 📱 AnnonceBTS – Application Mobile

[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Android](https://img.shields.io/badge/Platform-Android-green.svg)]()

---

## 🔹 Description

**annoncebts** est une application mobile destinée aux étudiants et enseignants de BTS.  
Elle permet de **créer, gérer et publier des annonces** pour les cours, examens, soutenances, stages et emplois du temps.  


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
