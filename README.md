# 🪐 Dailycosmos ![](https://img.shields.io/static/v1?style=plastic&label=Version&labelColor=212121&message=1.0.0&color=green) ![](https://img.shields.io/static/v1?style=plastic&label=Language&labelColor=212121&message=Kotlin&color=9719ff) ![](https://img.shields.io/static/v1?style=plastic&label=Technology&labelColor=212121&message=Android&color=#a4c639) ![](https://img.shields.io/static/v1?style=plastic&label=Backend&labelColor=212121&message=Firebase&color=ff9819) ![](https://img.shields.io/static/v1?style=plastic&label=Layout&labelColor=212121&message=XML&color=ff0068)
> ✨ My first app on Play Store. View as many photos as you can of the cosmos published by NASA since 1995. Create a user to save your favorites in the cloud or download them to your internal memory. 🌌.
> Download it [_here_][application]

## 🗂 Table of Contents
* [General Infomation](#general-information)
* [Technologies Used](#technologies-used)
* [Features](#features)
* [Architecture](#architecture)
* [Screenshots](#screenshots)
* [Material Theming](#material-theming)
* [Setup](#setup)
* [Usage](#usage)
* [Project Status](#project-status)
* [Room for Improvement](#room-for-improvement)
* [Acknowledgements](#acknowledgements)
* [Contact](#contact)
<!-- * [License](#license) -->

## General Information
It is an application that I developed personally in the summer of 2021. It took me about a month and a week to finish it. Much of the design is my own, based on Material Design metrics and recommendations. The layout was done in [Figma][figma_dailycosmos] software. 

Roughly speaking, it's an application where you can browse through all the photos published by NASA about the cosmos since 1995. 
You can create a user or log in anonymously to create one later. Once inside, browse chronologically, with the calendar or randomly 
through the thousands of photos that NASA has for you. If you like one in particular, you can like it and it will be saved in the cloud. 
You can also download the photo to your internal memory if you wish.

So, what are you waiting for? [Download it][application] and start your journey through the cosmos.

![figma_dailycosmos](https://raw.githubusercontent.com/CalixtoElProgramador/CalixtoElProgramador/master/daily_cosmos_components.jpg)
<br />
<br />
![](https://img.shields.io/static/v1??style=flat-squaren&label=Language&labelColor=212121&message=Kotlin&color=9719ff)
![](https://img.shields.io/static/v1??style=flat-squaren&label=IDE&labelColor=212121&message=AndroidStudio&color=9719ff)
![](https://img.shields.io/static/v1??style=flat-squaren&label=Architecture&labelColor=212121&message=MVVM&color=9719ff)
![](https://img.shields.io/static/v1??style=flat-squaren&label=Structure&labelColor=212121&message=CleanArchitecture&color=9719ff)
![](https://img.shields.io/static/v1??style=flat-squaren&label=Network&labelColor=212121&message=Retrofit2&color=9719ff)
![](https://img.shields.io/static/v1??style=flat-squaren&label=Serialization&labelColor=212121&message=GSON&color=9719ff)
![](https://img.shields.io/static/v1??style=flat-squaren&label=ImageLoading&labelColor=212121&message=Glide&color=9719ff)
![](https://img.shields.io/static/v1??style=flat-squaren&label=Cache&labelColor=212121&message=Room&color=9719ff)
![](https://img.shields.io/static/v1??style=flat-squaren&label=Preferences&labelColor=212121&message=Datastore&color=9719ff)
![](https://img.shields.io/static/v1??style=flat-squaren&label=Injection&labelColor=212121&message=Hilt&color=9719ff)
![](https://img.shields.io/static/v1??style=flat-squaren&label=Navegation&labelColor=212121&message=NavegationComponents&color=9719ff)
![](https://img.shields.io/static/v1??style=flat-squaren&label=Firebase&labelColor=212121&message=Authentification&color=ff9819)
![](https://img.shields.io/static/v1??style=flat-squaren&label=Firebase&labelColor=212121&message=Store&color=ff9819)
![](https://img.shields.io/static/v1??style=flat-squaren&label=Firebase&labelColor=212121&message=Storage&color=ff9819)
![](https://img.shields.io/static/v1??style=flat-squaren&label=Firebase&labelColor=212121&message=MLKitTranslator&color=ff9819)
![](https://img.shields.io/static/v1??style=flat-squaren&label=Firebase&labelColor=212121&message=Crashlytics&color=ff9819)
![](https://img.shields.io/static/v1??style=flat-squaren&label=Firebase&labelColor=212121&message=TestLab&color=ff9819)
![](https://img.shields.io/static/v1??style=flat-squaren&label=Permissions&labelColor=212121&message=Camara&color=#a4c639)
![](https://img.shields.io/static/v1??style=flat-squaren&label=Permissions&labelColor=212121&message=ReadInternalStorage&color=#a4c639)
![](https://img.shields.io/static/v1??style=flat-squaren&label=Permissions&labelColor=212121&message=WriteInternalStorage&color=#a4c639)
![](https://img.shields.io/static/v1??style=flat-squaren&label=Layout&labelColor=212121&message=XML&color=ff0068)
![](https://img.shields.io/static/v1??style=flat-squaren&label=Design&labelColor=212121&message=Figma&color=ff0068)
![](https://img.shields.io/static/v1??style=flat-squaren&label=Animations&labelColor=212121&message=Lottie&color=ff0068)

## Technologies used
- Android - version 1.0.0

## Features
- 👤 Log as a guest. If you have favorite photos, when you create your user they are saved in the cloud.
- 📦 The explanation and title can be translated using a trained Firebase model.
- 📆 Search for photos from a selected date range in the calendar.
- ☁ Your favorite photos are stored in the cloud.
- 🌙 Dark mode available.
- 🌞 A daily photo.
- 🌟 and more!

## Architecture
![](architecture.mvvm.png?raw=true)

## Screenshots
| Welcome | Login |  Register |
|:-:|:-:|:-:|
| ![Fist](screenshots/00.welcome.light.jpg?raw=true) | ![2](screenshots/01.login.light.jpg?raw=true) | ![3](screenshots/02.register.light.jpg?raw=true) |
| ![4](screenshots/00.welcome.dark.jpg?raw=true) | ![5](screenshots/01.login.dark.jpg?raw=true) | ![6](screenshots/02.register.dark.jpg?raw=true) |
| Today | Explore |  Favorites |
| ![7](screenshots/03.today.light.jpg?raw=true) | ![8](screenshots/04.explore.ligth.jpg?raw=true) | ![9](screenshots/05.favorites.light.jpg?raw=true) |
| ![10](screenshots/03.today.dark.jpg?raw=true) | ![11](screenshots/04.explore.dark.jpg?raw=true) | ![12](screenshots/05.favorites.dark.jpg?raw=true) |
| Calendar | Settings |  Info |
| ![13](screenshots/06.calendar.light.jpg?raw=true) | ![14](screenshots/07.settings.light.jpg?raw=true) | ![15](screenshots/08.info.light.jpg?raw=true) |
| ![16](screenshots/06.calendar.dark.jpg?raw=true) | ![17](screenshots/07.settings.dark.jpg?raw=true) | ![18](screenshots/08.info.dark.jpg?raw=true) |

## Material Theming
DailyCosmos uses Material Theming to customize the app’s color, typography and shape.

## Setup
![](https://img.shields.io/static/v1?style=plastic&label=Kotlin&labelColor=212121&message=1.5&color=9719ff) ![](https://img.shields.io/static/v1?style=plastic&label=MinSDKVersion&labelColor=212121&message=6.0&color=#a4c639) ![](https://img.shields.io/static/v1?style=plastic&label=AndroidStudio&labelColor=212121&message=4.2&color=green)

* For security reasons you must add your own Firebase configuration file `google-services.json` with Auth, Storage, FireStore, Crashlytics and Analytics active and the NASA API access keys.
* It is sufficient to open and run the project from Android Studio.

## Usage

## Project Status
Project is: _in progress_
<br />
<br />
Since I published it, I have not been able to make any progress in the development of this app. However, there are endless functions and features that I plan to add and polish. All this will be explained in the next section.

## Room for Improvement
* Download the image of the day in the background with Workmanager.
* Each time a new photo is released, send a notification.
* Remove the "Today" section and replace it with a more interesting one. Photos, news, things related to the cosmos.
* Improve the favorites interface.
* Make it possible for the user to edit their information, such as name, email or profile picture.
* Enable different ways to create a user, either Google or Facebook. 
* Create the process for the user to reset their password.
* Delete photos from the cache periodically. 
* Remove the search bar.
* Refactor a lot of code. Add Databinding and extract all the logic from the views.
* Clean up code.

## Acknowledgements
I thank NASA for providing this information free of charge through their API.

## Contact
You can send me an email or connect with me using the different options available on my home page.
<br />
email: aaron.calixto@outlook.com


[application]: https://play.google.com/store/apps/details?id=com.listocalixto.dailycosmo&hl=es_MX&gl=US
[figma_dailycosmos]: https://www.figma.com/file/RR1XH31BDa5Lgzw2trYe4G/DailyCosmos




