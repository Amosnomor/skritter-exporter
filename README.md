# skritter-exporter
Export Skritter studied words via Skritter API

Exports all studied words from Skritter in tab-separated, Anki importable format
matching the Anki note type I am using for Mandarin study.
Pinyin with tone numbers from Skritter is converted to Pinyin with tone marks.

## From Skritter:

*Simplified-TAB-Traditional-TAB-Pinyin(with tone numbers)-TAB-Definitions*

## Converts them to:

*Traditional-TAB-Simplified(empty if same as Traditional)-TAB-Pinyin(with tone marks)-TAB-Definitions*

## Building and testing
Maven project.

The unit tests mock all of the Skritter API access.

## Using with Skritter

In your Skritter account, at https://skritter.com/account/integrations
create a read-only access token.

Then create a src/main/resources/skritter.properties file containg:
```
#
# Skritter Access token.  Do not commit this. Listed in .gitignore
#
Bearer-Token=YOUR-SKRITTER-ACCESS-TOKEN
```