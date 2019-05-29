# QuranyApp

![logo](logo/horizontal.png)

Simple HolyQuran app

- [Google Play Link ](https://play.google.com/store/apps/details?id=education.mahmoud.quranyapp)
- [Youtube App Overview](http://bit.ly/2Ggw9Q7)
- [F-Droid link](https://apt.izzysoft.de/fdroid/index/apk/education.mahmoud.quranyapp) 



# Features 
- Go to specific sura by Scrolling and click.
- Go to specific sura by its Num.
- Go to specific sura by its Name.
- Go to last Read Position.
- Search for words or complete ayah.
- Download and Listen to ayahs
- Repeat each ayas and repeat whole listening
- Tafseer
- Points for Tasmee3 
- Upload Points and got Scoreboard 

# Challenges 
- **Data** 
first one was data, how to get data, first i found images of quran and build app using it but app size is too larg so i searched for text of quran i have found XML version of quran but i prefere JSON over xml then i got JSON version of quran and built 1.0 version of app.
- **Last Read Feature** 
its a good feature to save last position of read automatically I have faced problem with get scroll Position  but finally got it using ``` scrollView.getScrollY();   ``` and save it in sharedPreference, and scroll back using ```  scrollView.smoothScrollTo(0, scroll); ``` 

- **Improve Performance**
last method app load data from JSON and parse it with every open to **sura** so solution was use of db, i have used Room and build entities, Dao, and Database.

- **Search**
Search is fundamental feature of any Qurany App so I wrote Query to search in Ayahs but faced problem that Quran is dialacted(Tashkill - symbols) which make search impossible so I have searched and get a clean Version and add it beside last one (each ayah has two version one for display and other to search), data was XML from [Tanzil](http://tanzil.net/download) and use online converter to convert from XML to JSON but file was not as standard of JSON  so I have fixed it.

- **Listening**
Download audio have two ways first download whole sura or download seperate ayahs, i have used seperate ayahs to enable feature of listen to specific ayahs. I download audio in storage then store path into database to be used by medialPlayer.

- **Automatice Scroll down** 
was good feature but challegable I found a way for that by using **TimerTask** its job to scroll scrollView down by a constant num, but how to change rate with response to use? I create seekbar and after each change i make new Task but not this way has conflicts so i declare *attribute*  that changed by rate from user and used by **timertask**  

## Mistakes & Learn 

- I have a crash app on real device but app work correctly on emulator so I used a **crashReporter library** and it generates a report after check I found problem was with *primaryColor* that it has a *aplha value* so I removed alpha and app work correctly

- got error whrn build release app after use retrofit so i added some rules for proguard 
```
# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod
# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
# Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**
# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit
```
- base url of retrofit must end with / 


## Technologies
Qurany is built using:

Technology | Version
---------- | -------
Java | 8
XML | 1.0
Android Support Library | 28.0.0
ButterKnife | 8.8.1
gson | 2.8.5
sdp-android | 1.0.5 
ssp-android | 1.0.5
AppRate | 1.1
Room| 1.1.1
prdownloader | 0.4.0
easypermissions|1.1.1
stetho | 1.5.1
crashreporter | 1.0.9

# statistics

Languages | Line of code (LOC)
---------- | -------
Java | 7611
XML | 2811



# Screens

![img1](images/1.png)
![img1](images/22.png)
![img1](images/23.png)
![img1](images/3.png)
![img1](images/4.png)
![img1](images/5.png)
![img1](images/6.png)
![img1](images/7.png)
![img1](images/8.png)
![img1](images/9.png)
![img1](images/10.png)
![img1](images/11.png)



# Contributors 

- Thanks to [zularizal](https://github.com/zularizal) for awesome logo
- Thanks to [izzysoft](https://github.com/IzzySoft) for providing another installion source.

# Privacy Policy
- [link](privacy.md)
