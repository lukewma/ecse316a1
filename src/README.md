# ECSE 316 Assignment 1 Report 
### Group 46
February 14th, 2020
 Luke Ma					260745824
 Spencer Handfield			260xxxxxx

## Introduction
### Objectives
The goal of this assignment was to create a DNS client that could send out Datagram packets according to the UDP socket protocol and read through the corresponding response packet. This client had to be able to create its' own socket for each request, take in commandline flags such as timeout, max tries, port number and query type, as well as a target IPv4 address and a domain name. It would then parse the response for any errors and would print out a summary to the terminal display, interpreting them based on whether they contained A records (IP addresses) or CNAME records (DNS aliases). It also had to handle any errors that would come up and print them according to the format specified in the handout, with details about the exact cause of the error.

### Challenges
This was an incredibly difficult assignment, as we faced errors in multiple places: taking in command line flags, formatting the query question header with the right offsets and interpreting the response flags correctly. More specifically, the manipulation of byte arrays rather than simple strings proved to be much more complex than we initially expected. The compression was also a very large hurdle that we had to overcome.


### Results


## DNS Client Design
The overall design of our client rested on the idea that each class should only really be doing one thing, as per the tenets of object oriented programming. We assigned the main method and the command line handling to Client, the formatting of the query datagram to Request, the parsing of the response packet to Response and the formatting of the output for the terminal display to Record. 


### Client
In our Client, we utilized a variety of methods to offload the work from our Main, such as a pollReq to setup a socket for each UDP packet and keep track of the attempts, a mkOpt and a cOpt to deal with the flags entered by the User, a parseIPDom for the remainder of the arguments. While we originally wanted to use an external library for our command line argument parsing, we have since regretted the decision, owing to the meagre implementation of the library and its' supplemental complexity. We chose the Apache commons cli


### Request


### Record


### Response


### QType


## Testing
### Command Line Arguments


### Request Packet Integrity


### Request-Response Congruity


### Query Type Correctness


### Exception Handling

## Experiment
1. What are the IP addresses of McGill’s DNS servers? Use the Google public DNS server (8.8.8.8) to perform a NS query for mcgill.ca and any subsequent follow-up queries that may be required. What response do you get? Does this match what you expected?

2. Use your client to run DNS queries for 5 different website addresses, of your choice, in addition to www.google.com and www.facebook.com, for a total of seven addresses. Query the seven addresses using both a McGill DNS server (132.206.85.18) and the Google public DNS server (8.8.8.8).

**_Figure 1:_** _Answers & Additional Info from the 7 Tested Websites_

 Website Address | McGill DNS Output | Google DNS Output 
:---: | :---: | :---:
 www.google.com |
 www.facebook.com |
 www.youtube.com |
 www.github.com |
 www.twitter.com |
 www.grailed.com |
 www.lmgtfy.com |


3. Do the responses from both servers match? If not, how do they differ? Speculate on why they might differ, and propose additional experiments you could conduct to further explore your hypothesis.


## Discussion

## References

## Appendices
