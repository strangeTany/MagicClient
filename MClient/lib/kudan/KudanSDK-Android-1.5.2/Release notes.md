# Release notes 
# Kudan Android Framework 1.5.2
- - - -
* Updated Kudan CV
* All singletons must now be initialised before use.

# Kudan Android Framework 1.5.1
- - - -
* Updated documentation
* Fixed issue with ARGyroManager world visibility (KUDAN3D-130)

# Kudan Android Framework 1.5
* Extensive update to the documentation of the whole framework
* ARModelNode’s `getNode()` now returns an ARModelNode rather than ARNode (KUDAN3D-117)
* ARModelNode’s `reset()` now sets `isPlaying` to `false`, rather than the previous of `true` (KUDAN3D-117)
* ARNode now has a constructor that only takes a String to use as the node’s id.
* Significantly reduced the amount of logs that are produced by KudanCV. You should be able to read what’s in your `logcat` yet again!

# Kudan Android Framework 1.4.2
- - - -
- Added ARBillboardNode - you can now make your object always face you! (Make sure to set the forward direction and lock the axis you don't wish to update)
- Improved the playback of alpha videos
- Calling `getFullTransform()` on ARNode now ensures that the underlying world transforms are updated first

# Kudan Android Framework 1.4.1
- - - -
- Added the screenshot() function to ARView
- Implemented the pause() method on ARVideoTexture. You can now pause your ARVideoNode and ARAlphaVideo nodes!

# Kudan Android Framework 1.4
- - - -
- The library is now released as a .AAR file, to match the wider Android community
- Extended Marker functionality has been added. To make your marker extendable simply set `setExtensible(true);` on your ARImageTrackable after it has been loaded
- Added methods: findChildByName, addChildren, translateByVector, scaleByVector, rotateByRadians to ARNode
- Added method to return application context from ARActivity
- Added method for returning the Asset Manager to ARRender
- Added methods: getNumberOfIndicies, setVertexBuffer,getVertexDataStride to ARMeshNode
- Added methods: getSrcHeight and getSrcWidth to ARExtractedCameraTexture
- Added methods: init, initWithPackagedFile, initFromPath to ARVideoNode and ARAlphaVideoNode
- Altered how singletons are initialised, so that initialise doesn't need to be called when you first get an instance of your class. e.g. ArbiTrackerManager.getInstance().initialise() -> ArbiTrackerManager.getInstance()
- Other various stability fixes 🏥 😉

New API License Keys are being introduced with this release. Please check https://wiki.kudan.eu/Development_License_Keys for new license keys

# Kudan Android Framework 1.3.1
- - - -
- Added Marker Recovery Mode
- Built with libpng v.1.5.27 so that Google Play likes us again
- Really fixed loading Texture2D from path
- Fixed an issue with content not loading properly after a scene has been setup (KUDAN3D-73) 

# Kudan Android Framework 1.3
- - - -
- Improvements to marker detection speed
- Fixed loading Texture2D from path
- Removed some pesky bugs to make the framework even more stable

# Kudan Android Framework 1.2.4
- - - -
- Added ARCameraStreamListener interface to receive camera frame data after processing.
- ARGyroPlaceManager floor height is now settable.
- Added fade in time to ARVideoTextureMaterial.
- Fixed tracking lag in all trackers. Tracking should now be more responsive.
- Added ARPointNode, a class that renders updatable sprites on screen.
