Decision 001
------------
All services use UUID as primary keys.

Reason:
Distributed systems, security, scalability.

------------------------------------------------

Decision 002
------------
Services communicate using UUID, never username.

Reason:
Usernames can change.

------------------------------------------------

Decision 003
------------
Email belongs to User Service.

Reason:
Identity data belongs to User Service.
Authentication data belongs to Auth Service.

------------------------------------------------

Decision 004
------------
Usernames can be changed a limited number of times.
Username history is retained.

------------------------------------------------

Decision 005
------------
Accounts use soft delete with a 30-day recovery window.