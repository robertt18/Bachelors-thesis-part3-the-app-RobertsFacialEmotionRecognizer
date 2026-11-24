# RobertsFacialEmotionRecognizer
# Facial Emotion Recognizer - Android App
## Lucrare de licență - Mindroc Robert-Andrei, ETTI UPB 2026

## Descriere
Aplicație Android pentru recunoașterea facială a emoțiilor folosind 
modele deep-learning în format LiteRT (TFLite).

## Funcționalități
- Selectarea unuia din cele 4 modele disponibile
- Încărcarea imaginilor din galerie
- Afișarea distribuției probabilităților pentru 7 emoții
- Afișarea latenței de inferență
- Funcționează offline (fără conexiune internet)

## Modele integrate
| Model | Bază de date | Acuratețe |
|---|---|---|
| MobileNet | FER-2013 augmented | 80.91% |
| V-CNN | CK+ | 90.54% |
| V-CNN | FER-2013 original | 59.96% |
| VRES-CNN | RAF-DB | 76.08% |

## Tehnologii folosite
- Kotlin + Jetpack Compose
- TensorFlow Lite / LiteRT
- Android Studio Panda

## Dispozitive testate
- Motorola Moto G60
- Samsung Galaxy A12
- Motorola Edge 30 neo
- Samsung Galaxy A54

## Emoții detectate
Angry, Disgust, Fear, Happy, Neutral, Sad, Surprise, Contempt

Rezultate importante

<img width="911" height="473" alt="Screenshot 2026-11-24 230948" src="https://github.com/user-attachments/assets/bf8cbe7d-3854-4891-8b6e-002f9c089880" />

<img width="912" height="514" alt="Screenshot 2026-11-24 231037" src="https://github.com/user-attachments/assets/a35b9a82-6aac-4b95-b07d-98a763154af4" />

<img width="931" height="513" alt="Screenshot 2026-11-24 231052" src="https://github.com/user-attachments/assets/ee2ce7bf-e96b-4e8c-a528-3f279e824629" />

