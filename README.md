# Find Me A Mechanic

Final year project for BSc Business Informatics

This is an application which connects repairmen, technicians with people in need of technical maintenance.
People can sign up to the application (which runs on Firebase Firestore database) and report household problems like 
leaky, broken pipes, runny toilet, electrical problems with television, pc etc.
Registered technicians can apply to reported problems and the client with the problem chooses one out of them to fix their problem.
During the maintenance job the application generates a service worksheet which the repairman fills out and when it is uploaded by them, the job gets closed.

App design: https://xd.adobe.com/view/b815c980-72c5-4d7c-42f0-52d64e599339-ce24/grid/

Functions:
- Registration/Login (even with Google account too) as a Client / Repairman
- Forgotten password request
- Clients can upload tickets (jobs) within 15 km of their location and they also are able to modify these tickets
- Repairmen can apply to these tickets
- Clients are able to see who has applied to their tickets and select the one who does the job
- Repairmen can search jobs within the distance they specify from their current location and they can filter jobs by type
- Clients and repairmen are able to chat within a ticket
- Clients can start a phone call with the repairmen
- When a job is finished the repairman fills out the worksheet -> pdf file created which can be downloaded any time
- Users are able to modify their registration data or delete their profile
- Notifications if someone new applied to a ticket / client selected the final repairman who does the job
