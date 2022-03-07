# No està en desenvolupament!! / Not in development!!
[Go to English version](#adictic---english)
# AdicTIC - Català

Una aplicació de control parental amb suport psicològic per control·lar les addiccions a noves tecnologies desenvolupat per a Android.

Desenvolupat amb [Masfa98](https://github.com/Masfa98/). El servidor de l'app es troba a https://github.com/Masfa98/AdicTIC_Server.

## Estructura
- **App** - aplicació principal. Permet contactar amb professionals i veure informació detallada sobre les addiccions. Conté dues variants:
  - **Tutor** - permet control·lar remotament el dispositiu del menor i extreure'n dades d'ús
  - **Menor** - extreu informació del dispositiu i la puja al servidor per poder-se veure remotament des del dispositiu del tutor. També executa les funcionalitats de control parental (bloqueig del dispositiu/apps, horaris...)
- **Admin** - aplicació per als professionals. Poden rebre consultes dels clients i obrir xats amb ells o programar videotrucades per tenir sessions privades. També poden veure les dades extretes per dispositius mòbils sempre i quan el tutor hagi permès l'accés. També permet editar informació sobre l'oficina per consultes en persona o el perfil del professional.
- **Common** - llibreria amb totes les funcions/classes comunes entre les aplicacions **App** i **Admin**.

## Funcionalitats
- **Visualització de dades d'ús** - utilitza sobretot [UsageStatsManager](https://developer.android.com/reference/android/app/usage/UsageStatsManager)
  - **Informe mensual**
  - **Dades d'ús d'aplicacions diari**
  - **Costums de mòbil** - cops que s'ha desbloquejat, cops que s'ha entrat a cada app...
- **Funcions de control parental** - utilitza sobretot un [servei d'Accessibilitat](https://developer.android.com/reference/android/accessibilityservice/AccessibilityService.html) i [Workers](https://developer.android.com/topic/libraries/architecture/workmanager)
  - **Bloqueig d'aplicacions** - per temps o indeterminat
  - **Bloqueig del dispositiu** - per temps o indeterminat
  - **Horaris nocturns** - bloquejar el dispositiu per anar a dormir i desbloquejar a l'hora de despertar-se
  - **Bloqueig d'events** - durant hores concretes en dies determinats
- **Notificacions** - utilitzem [Firebase](https://firebase.google.com/docs/cloud-messaging/)
- **Contacte amb professionals**
  - **Enviar una consulta** - permet obrir un xat (pendent d'utilitzar [API de Signal](https://github.com/signalapp/libsignal-client))
  - **Videotrucada** - utilitzant [Jitsi](https://jitsi.org/api/)

## Branca `room`
Busquem millorar el funcionament intern utilitzant diverses noves tecnologies:
- **[Room](https://developer.android.com/jetpack/androidx/releases/room)** - guardar les dades de bloqueig i estat en una base de dades dins el mateix dispositiu fill
- **[Hilt](https://developer.android.com/training/dependency-injection/hilt-android)** - injecció de dependències amb Hilt
- **RxJava** - conjuntament amb `Room`, `Hilt` i `Retrofit2`

# AdicTIC - English

An parental control application with psychological support to help addictions developed for Android.

Developed with [Masfa98](https://github.com/Masfa98/). The app's server is in https://github.com/Masfa98/AdicTIC_Server.

## Structure
- **App** - main application. Allows the user to contact professionals and read detailed information about addictions. It has two variants:
  - **Tutor** - allows the user to control the child's device remotely and extract usage data
  - **Child** - extracts usage information and uploads it to the server to be able to be seen from the tutor's device. It also executes the parental control functionalities (app/device block, schedules...)
- **Admin** - application for professionals. Allows users to receive inquiries from clients and chat with them or schedule videocalls for private sessions. They can also access to the minor's usage information if allowed by the tutor. There's a user profile and an office profile they can edit to advertise themselves.
- **Common** - library with all the functions/classes the **App** and **Admin** applications have in common

## Features
- **Visualize usage data** - uses [UsageStatsManager](https://developer.android.com/reference/android/app/usage/UsageStatsManager)
  - **Monthly report**
  - **Daily app usage data**
  - **Usage customs** - times it has been unlocked, times it has accessed an app...
- **Parental control** - uses [Accessibility Service](https://developer.android.com/reference/android/accessibilityservice/AccessibilityService.html) i [Workers](https://developer.android.com/topic/libraries/architecture/workmanager)
  - **Block applications** - time limit or indeterminate
  - **Block device** - time limit or indeterminate
  - **Night schedule** - block the device during sleeping times
  - **Event blocking** - block during events at certain times during a week
- **Notifications** - uses [Firebase](https://firebase.google.com/docs/cloud-messaging/)
- **Contact with professionals**
  - **Send an inquiry** - allows to open a chat (future: use [Signal API](https://github.com/signalapp/libsignal-client))
  - **Videocall** - uses [Jitsi](https://jitsi.org/api/)

## Branch `room`
We are looking to improve internal functionalities with new technologies:
- **[Room](https://developer.android.com/jetpack/androidx/releases/room)** - store parental control data in a database inside the minor's application
- **[Hilt](https://developer.android.com/training/dependency-injection/hilt-android)** - dependency injection
- **RxJava** - together with `Room`, `Hilt` and `Retrofit2`
