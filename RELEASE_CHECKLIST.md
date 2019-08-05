## Release Checklist
1. Update the app version name in `app/build.gradle`.

2. Update the app version code in `app/build.gradle`.

3. Update the app version name in the `strings.xml` for the "About" activity.

4. Check for any TODOs that need to be completed before releasing.

5. Verify that all of the modules are working:

  a) Home - nearby food locations, check the weather, valid and invalid course lookup

  b) Food Services: Locations - the entire location list, an individual location

  c) Food Services: Menus - the week dialog, the entire menu list, an individual menu

  d) Food Services: Notes - the week dialog, the entire notes list, an individual note

  e) Courses: a course list for a subject, open in browser icon, info, prerequisites, schedule, exams

  f) Campus: Points of Interest - map type dialog, selecting different pois, layers menu icon, map interactions

  g) Campus: Buildings - the entire buildings list, an individual building, layers menu icon

  h) Campus: Parking - selecting a parking lot on the map, layers menu icon,  map interactions

  i) Campus: Goose Watch - nest location, layers menu icon, map interactions

  j) News: News - the entire news list, an individual news item, open in browser button

  k) News: Events - the entire events list, an individual events item, open in browser button

  l) Weather: scrolling up and down

  m) Resources: Sites - the entire sites list, opening an site with the in-app browser

  n) Resources: Sunshine List - the entire sunshine list, sorting the sunshine list

  o) About - email and leave a review

6. Repeat step 4 with a few modules but with the internet turned off.

7. Generate a signed APK of the release variant with both the V1 Jar signature and the V2 full APK signature.

8. Upload the APK with the path `app/release/app-release.apk` to the Google Play Console.

9. Update the release notes in the Google Play Console and remember to use the latest version name.

10. Release the app!