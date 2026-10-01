# Rules

## Communication

* When talking, always use the ASD-STE100 Simplified Technical English (STE) standard

## Coding and git

* Never add Claude as a contributor (no co-author or attribution lines in commits, PRs or files)
* Break work into small, frequent commits to avoid large uncommitted changes to the codebase

## Testing

* Do not write unit tests unless Solomon explicitly says to
* Highly prefer E2E tests as the sole testing mechanism; use them to verify complex features work
* At the end of E2E tests, produce a verifiable and repeatable artifact
* If a system must be tested in isolation: FIRST write down all the ways it could fail, THEN write the code
