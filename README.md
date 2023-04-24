# Tartan - Group 12

## Group Members
| Members | CCID |
| :---: | :---: |
| S M Navid Amin | smnavid |
| Abenezer Belachew | belachew |
| Tina Do | tdo |
| Andreea Muresan | amuresan |

## Building

The build instructions can be found [here](./docs/build_instructions.md).

## System description

System description can be downloaded as a pdf file
[here](./docs/TartanSystemDescription.pdf).

## Folder structure

The entire system (Tartan Smart Home Service) resides in *smart-home/*
directory.

Please see the system description (docx) file for more detailed information
about Tartan's design, architecture, requirements, etc.

## Continuous deployment

Continuous deployment is done through GitHub Actions. The action runs every time a branch is merged with master.

## Revert

### Versioning:

When creating a pull request, include one of the following anywhere in the commit message (including the #):

* #major
* #minor
* #patch

The default is none. If none are included, *a tag will not be created*. **Reverts can only be done on commits with tags.**

### To revert to a previous version:

* Copy the version tag of the desired merge
* Go to GitHub Actions
* In the action  *Revert Version*  click  *Run Workflow*
* Paste the version into the  *Version*  text field and run the workflow

