# CalendarSync
A repository for syncing an Exchange calendar to a google calendar

Reads all appointments from an exchange calendar and inserts them into a Google calendar. It checks if appointments already exist and adds them if they do not. If appointments have been changed (see MyEvent.equals()) or deleted in Exchange it will remove them.

So far, a very hacky time-zone support (only CET & CEST).

Uses cryptography to not store passwords in plain text in the source:
  exchange.pwd, first line username, second line password as created with the Crypt class
  client_secret_google.json, for accessing the google API (created online from the respective google account)
