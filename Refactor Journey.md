# Cases 

```
 Java :
 // traverse ayahs to check if it downloaded or not
  for (ayahItem in ayahsToListen) {
      if (ayahItem.audioPath == null) {
          ayahsToDownLoad.add(ayahItem)
      }
  }

  Kotlin:  ayahsToDownLoad = ayahsToListen.filter { it.audioPath == null }
```

  
