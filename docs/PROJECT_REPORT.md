# Project Report: Alhemdrew’s Course Management System

**Course:** COS 201 — Programming I

**Project type:** Java desktop application

## Introduction

Alhemdrew’s Course Management System is a Java Swing application for organizing a student’s courses for a semester. It records each course’s code, title, and credit-unit value, and provides a graphical way to review and search those records. Course information can be saved to and loaded from a UTF-8 text file. The project applies introductory programming concepts to a practical student task.

## Objectives and Functionality

The project was developed to demonstrate object-oriented programming, methods, collections, decision-making, loops, recursion, validation, exception handling, and file input/output. The interface provides a dashboard with a course count and total-unit summary, an all-courses table with filtering, a course-entry form, and a course-code search view. Save and Load controls connect the interface to the existing course persistence methods. Because the project data file was absent, first-run initialization created this illustrative sample set once:

| Course code | Course title | Units |
| --- | --- | ---: |
| COS 201 | Programming I | 3 |
| MTH 201 | Discrete Mathematics | 3 |
| CSC 205 | Data Structures | 4 |

These are project examples, not university-provided course records. Existing, empty, malformed, or unreadable files are not replaced with sample data.

The course form validates required text and positive whole-number units. Course codes are trimmed and normalized to uppercase. Duplicate codes are rejected without changing the course list. The interface reports validation and file-operation results and warns before replacing unsaved memory data or leaving the application with unsaved changes.

## Object-Oriented Design

`Course` models a single record and validates its code, title, and credit units. Its private fields and public getters provide controlled access to course data.

`CourseManager` owns the in-memory collection and provides methods to add courses, return the records, search by normalized code, save and load records, and calculate total units. This separation keeps data rules out of the Swing event handlers.

`Main` starts the graphical application on Swing’s event-dispatch thread. `MainFrame` builds the navigation, dashboard, tables, search, form, status feedback, and confirmations. `AppTheme` centralizes shared colors, fonts, button styles, and text-field styling. File work is performed with `SwingWorker`, keeping disk operations off the interface thread.

## Recursive Calculation

Total credit units are calculated in `CourseManager` by a recursive helper that receives the course list and an index. The base case returns zero when the index reaches the list size. Otherwise, the method adds the current course’s units to the result for the next index. This handles an empty collection and demonstrates recursion directly rather than using a loop for the total.

## File Handling and Validation

The data file is `data/courses.txt`, using one UTF-8 record per line in the form `COURSE_CODE|Course title|credit units`. The pipe delimiter and line breaks are rejected in course codes and titles so records cannot be split ambiguously. Saving writes a temporary file and replaces the saved file after the write completes. Loading first reads and validates rows into a temporary list, accepts only one record per normalized course code, and then replaces the current list after the read completes. Malformed and duplicate rows are counted. Missing and failed reads are reported, and failed reads preserve the in-memory collection. Exceptions from file operations are surfaced as application status messages.

## Implementation Considerations

A central implementation consideration is keeping the dashboard, table, search result, and saved-state indicator synchronized with one course collection. The interface refreshes its table models and metrics from `CourseManager` after operations. A second consideration is protecting user data during startup and loading: sample initialization is conditional on a missing file, while load replacement and save-over-malformed-file actions are made explicit. The recursive method is retained in the manager as the source of the dashboard total.

## Verification and Conclusion

The project compiled with `javac -d out src/*.java`. EDT-driven Swing integration checks in temporary working directories verified first-run initialization, valid and invalid course form submissions, duplicate rejection, dashboard count and unit updates, case- and whitespace-tolerant search, course-table filtering, saving, and loading after opening a fresh manager. File-safety checks verified malformed-row reporting, preservation of the source file during load, cancellation and confirmation of replacement, preservation of in-memory records after unreadable reads, truthful failed-save feedback, atomic replacement, and empty-list saving. The application was launched on the available X11 desktop and its Java window was inspected from targeted screenshots at the default and minimum supported sizes. The real project data file was initialized only after inspection confirmed it did not already exist.

The application brings course data, validation, recursion, persistence, and a desktop workflow together while keeping the model, manager, and interface responsibilities distinct. It provides a suitable foundation for demonstrating the COS 201 concepts through a small, understandable Java project.
