# Find Me A Mechanic

Final year project for BSc Business Informatics

This is an application which connects repairmen, technicians with people in need of technical maintenance.
People can sign up to the application (which runs on Firebase Firestore database) and report household problems like 
leaky, broken pipes, runny toilet, electrical problems with television, pc etc.
Registered technicians can apply to reported problems and the client with the problem chooses one out of them to fix their problem.
During the maintenance job the application generates a service worksheet which the repairman fills out and when it is uploaded by them, the job gets closed.

App design: https://xd.adobe.com/view/b815c980-72c5-4d7c-42f0-52d64e599339-ce24/grid/

Functions created so far:
- Registration/Login (even with Google account too) as a Client or Repairman
- Forgotten password request
- Clients can upload tickets (jobs) within 15 km of their location and they also are able to modify these tickets
- Clients are able to see who has applied to their tickets
- Repairmen can search jobs within the distance they specify from their current location
