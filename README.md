
**Project Overview**

This Android application is designed to monitor permission usage in real time.

It runs a background service that actively tracks which permissions are being accessed by apps the user opens — starting from the moment the service is launched. 

The monitoring does not apply retroactively;

only apps accessed after the service starts are considered.

The app focuses on four sensitive permission types:

Location, Camera, Microphone, and Contacts.

Once the service is running, it detects whenever an app uses one or more of these permissions, and records the usage for display and analysis within the app’s UI.

 **Dashboard Fragment (Main Screen)**
 
The Dashboard fragment provides a visual overview of real-time permission usage.

It features a pie chart that displays the percentage distribution of each permission type (Location, Camera, Microphone, Contacts).

It also shows the total number of unique permission accesses detected since the service started.

To avoid duplicate counting, each app-permission combination is counted only once per session.

For example, if an app like Waze uses the location permission, it is counted only once, even if the user reopens the app without closing it first. 

A new count will only be registered if the app is fully closed and reopened!

Additionally, a table displays the five most recent permission access events, sorted by timestamp. 

This helps users quickly identify which apps recently accessed sensitive permissions.


** Logs Fragment**

The Logs fragment displays a complete list of all permission access events that have occurred since the monitoring service was activated.

This log is continuously updated in real time as the user interacts with other apps.

Each entry in the log includes the following details:

App Name – the name of the application that accessed a permission

Timestamp – the exact time the permission was accessed

Permission Type – the specific permission used (e.g., Location, Camera, Microphone, or Contacts)


**Risks Fragment**

The Risk fragment presents a bar chart that visualizes the number of sensitive permissions accessed by each app. 

This allows users to quickly assess which apps may pose higher risks based on their permission usage.

X-Axis: Displays the names of the applications that accessed permissions.

Y-Axis: Ranges from 0 to 4, representing the number of unique sensitive permissions used by each app. Only whole numbers are shown.

Bar Height: Indicates the number of distinct permissions accessed by each app.

Bar Color: Represents the risk level associated with the app:

🟩 Green (Low Risk): 1 permission accessed

🟧 Orange (Medium Risk): 2–3 permissions accessed

🟥 Red (High Risk): 4 permissions accessed

Apps that have not accessed any permissions are excluded from the chart. 

This visualization provides a quick and intuitive overview of potential risks across installed applications.




