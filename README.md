ctf-writeup-crawler
======================
Crawler for CTF Writeups

http://www.postgresql.org/
Please ensure the PostgreSQL is installed and running on machine with the following configured
 * Ensure that there is a database named: ctfcrawler
 * Ensure that there is a postgres user/password: ctfcrawler/ctfcrawler


The program is developed on Eclipse IDE.
To run, either import folder to Eclipse and run using Eclipse
or extract the jar file using "jar xf <jarFile>" and run using "java <classFile>"


WebCrawlerDriver
----------------
The driver class to start the WebCrawler. To run, "java WebCrawlerDriver"


DatabaseFrontend
----------------
The Database Frontend for queries. To run, "java DatabseFrontend"


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