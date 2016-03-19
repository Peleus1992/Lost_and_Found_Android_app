# Lost_and_Found_Android_app
Georgia Tech students move around school with many objects: cell-phones, notebooks, keys, credit cards, buzzcards, etc. During the typical fast paced routine of a day on campus, students will often misplace or lose objects of important value without noticing right away. While we believe that our campus is relatively safe, and trust people who find lost objects to try to return those objects to their owners, or to leave them with security or at an administration desk, we also believe that many objects would still remain "un-found" due to the huge size of our campus. We have researched ideas that tried to help find lost objects, and we found few but great products, such as the Tile app [1] that use RFID trackers to tag objects of importance. These ideas, however, require the purchase of expensive devices and attaching them to objects. These devices may be too expensive for students on low-budget. Additionally, some objects that can be lost, such as a pair of glasses, can not be conveniently attached to a RFID tracker. To solve the problem of lost objects, we propose creating an app where students can report losing and finding objects on campus.

This is originally a project for class CS6235 which I worked with Katri Mohamed. I was in charge of the Android client part while Katri took care of server part. It was very happy to work with this nice buddy and I learnt a lot from him. He graduated that semester,  so I have to continue work on the project as a open source project as promised in the project proposal.

Although we have implemented many functionalities and the code can run successfully, many redundant code exists and there's no proper comments. Besides, many warning exists and the code is not fully tested, so I believe there are many bugs to be explored. Moreover, there are many other interesting functions to be implemented. As a result, I plan to continue working on this project during next semester.

## Function
### Report lost and found
Users can report lost and found by clicking on the floating action button. They need to select report type (lost/found). Then they need type in title and description to describe the object. They can type in keywords to help us match lost and found report. They can also take a picture of the object. After that, users can select a time when they found or lost the object. They can also pin point a location on Google Map or search location by text. After that they only need to click on Done button to submit.

<img src="https://github.com/Peleus1992/Lost_and_Found_Android_app/blob/master/screenshot/Screenshot_report1.png" height="420" width="240"/>
<img src="https://github.com/Peleus1992/Lost_and_Found_Android_app/blob/master/screenshot/Screenshot_report2.png" height="420" width="240"/>


###  Lost report list
This page shows a list of lost reports. User can scroll down refresh and scroll up to see more reports. User can comment on the report, share on Google+ and Contact the publisher by emailing. Clicking on the report can see detailed information.

<img src="https://github.com/Peleus1992/Lost_and_Found_Android_app/blob/master/screenshot/Screenshot_lost.png" height="420" width="240"/>

###  Found report list
This page shows a list of found reports. User can scroll down refresh and scroll up to see more reports. User can comment on the report, share on Google+ and Contact the publisher by emailing. Clicking on the report can see detailed information.

<img src="https://github.com/Peleus1992/Lost_and_Found_Android_app/blob/master/screenshot/Screenshot_lost.png" height="420" width="240"/>

### My report list
This page shows a list of my reports. User can scroll down refresh and scroll up to see more reports. Users can delete the report. They can also mark their lost report as found or mark their found report as returned. Clicking on the report can see detailed information.

<img src="https://github.com/Peleus1992/Lost_and_Found_Android_app/blob/master/screenshot/Screenshot_mypost.png" height="420" width="240"/>

### Comment on report
By clicking on Comment button, user can see others comment on the report and can comment on the report.

<img src="https://github.com/Peleus1992/Lost_and_Found_Android_app/blob/master/screenshot/Screenshot_comment.png" height="420" width="240"/>

### Contact the publisher
By clicking on the Contact button, user can contact the publisher by emailing.

<img src="https://github.com/Peleus1992/Lost_and_Found_Android_app/blob/master/screenshot/Screenshot_contact.png" height="420" width="240"/>

### Share on Google+
By clicking on the Share button, user can share the report on Google+.

<img src="https://github.com/Peleus1992/Lost_and_Found_Android_app/blob/master/screenshot/Screenshot_share.png" height="420" width="240"/>

### Search reports
User can search reports by clicking on the search icon.

<img src="https://github.com/Peleus1992/Lost_and_Found_Android_app/blob/master/screenshot/Screenshot_search.png" height="420" width="240"/>

### Menu Page
By swipe right, user can see the menu page.

<img src="https://github.com/Peleus1992/Lost_and_Found_Android_app/blob/master/screenshot/Screenshot_menu.png" height="420" width="240"/>

### Setting Page.
This page displays user's account information and allow user to log out.

<img src="https://github.com/Peleus1992/Lost_and_Found_Android_app/blob/master/screenshot/Screenshot_settings.png" height="420" width="240"/>

### Feedback
By clicking on the feedback button, user can give us feedback.

<img src="https://github.com/Peleus1992/Lost_and_Found_Android_app/blob/master/screenshot/Screenshot_feedback.png" height="420" width="240"/>

### Match reports
User can specify one or more keywords and location to help us match lost report and found report. Basically, for example. User A lost a Nexus 6p, he specify a keyword "Nexus 6p" and the location. When user B found a Nexus, he also sepcify keywords "Phone", "Nexus 6p" and location. So when the keywords set of lost report and found report share one or more keywords and the distance between lost report and found report is within  50 meter, the backend will find a match and send notification to user. 

<img src="https://github.com/Peleus1992/Lost_and_Found_Android_app/blob/master/screenshot/Screenshot_match.png" height="420" width="240"/>
