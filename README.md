# ctf-writeup-crawler
Crawler for CTF Writeups

Please ensure the PostgreSQL is installed and running on machine with the following configured
 * Ensure that there is a database named: ctfcrawler
 * Ensure that there is a postgres user/password: ctfcrawler/ctfcrawler
http://www.postgresql.org/
 
 The program is developed on Eclipse IDE.
 To run, either import folder to Eclipse and run using Eclipse
 or extract the jar file using "jar xf <jarFile>" and run using "java <classFile>"

"java WebCrawlerDriver" to start the WebCrawler
"java DatabseFrontend" to perform queries on database


WhiteList_Domains.txt
----------------------
Contains domains that is allowed during crawling 


Categories.txt
---------------
Contains the categories to tag the links during crawling


Potential_WhiteList_Domains.txt
-------------------------------
Contains new domains found during crawling that might be suitable to be whitelisted


Potential_Categories.txt
-------------------------------
Contains new categories found during crawling that might be suitable for tagging