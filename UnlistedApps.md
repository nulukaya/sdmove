# Introduction #

Not all applications will be listed in SDMove, only the ones that have explicitly declared their level of support for being moved to external storage.

# Details #

An application in Red means it's been explicitly denied, rather than passively denied by simply not making any decision at all.

To be more clear, the Froyo API added a new flag indicating where apps can be stored.  If the app doesn't set the flag at all, Android does not allow the app to be moved.  This will be the case for any applications that have not been updated since Froyo was released, but the flag is not required, so there's no guarantee that it will be there for applications updated since then, either.

If the flag is not set, it will not (currently) be listed in SDMove.  An application in Red means that the author specifically set the flag to disallow moving the application to external storage.  The Android developers' guide lists a [number of reasons](http://developer.android.com/guide/appendix/install-location.html#ShouldNot) that an application might require internal storage.