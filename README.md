# Alhemdrew’s Course Management System

**Student Course Management | COS 201 — Programming I**

A Java Swing desktop application that helps students record, review, search, and persist semester course information and calculate the current credit-unit total.

## Problem and Objectives

Students need a simple way to keep course codes, titles, and credit units organized for a semester. This project applies introductory programming concepts to that task through a small desktop application.

The objectives are to practice object-oriented design, use collections to manage course objects, validate input, provide a graphical workflow, calculate totals recursively, and preserve course records through text-file input and output.

## Features

- Dashboard metrics and course table sourced from the current `CourseManager` collection.
- Add a course with required-field, integer-unit, positive-unit, delimiter, and duplicate-code validation.
- Browse all courses and filter the table by code, title, or units.
- Search by course code while ignoring surrounding whitespace and letter case.
- Calculate total credit units using the manager's recursive method.
- Save and load records through visible desktop controls, with status feedback and replacement confirmation for unsaved changes.
- On first launch only, initialize documented sample records if `data/courses.txt` does not exist. Existing, empty, malformed, or unreadable files are not replaced with samples.
- Warn before exiting with unsaved in-memory course changes.

If the data file is missing on first launch, the application saves these three illustrative records once:

| Course code | Course title | Credit units |
| --- | --- | ---: |
| COS 201 | Programming I | 3 |
| MTH 201 | Discrete Mathematics | 3 |
| CSC 205 | Data Structures | 4 |

These are sample project records, not courses supplied or approved by the university. Users can add their own records and replace the sample list through normal application actions. Samples are not re-created when a file already exists, even if it is empty or cannot be read.

## Technologies

- Java and the standard JDK
- Swing and AWT for the desktop interface
- Java collections for in-memory records
- UTF-8 text-file handling through `java.nio.file` and buffered readers/writers
- No external libraries, database, or network access

## Design

`Course` represents one course and enforces nonblank code/title values, delimiter-safe text, uppercase trimmed course codes, trimmed titles, and positive credit units.

`CourseManager` owns the course collection and implements duplicate detection, normalized course-code search, defensive list retrieval, persistence, and recursive unit calculation. The recursive total uses an index-based helper: it returns zero at the end of the list, otherwise adds the current course’s units to the result for the remaining courses.

`Main` starts the Swing interface on the event-dispatch thread. `MainFrame` builds the window, navigation, dashboard, course table, filtering/search experience, add form, persistence feedback, and confirmations. File reads and writes run through `SwingWorker` so ordinary disk operations do not block interface updates. `AppTheme` holds shared styling choices for buttons, colors, typography, and inputs.

## Validation and Error Handling

The form checks required values and parses credit units before constructing a `Course`; the model remains the authority for course-data constraints, and `CourseManager` rejects duplicate normalized codes. Validation feedback appears beside the form. Search reports blank input and missing courses. File exceptions are translated into visible status messages, and a failed read does not replace the in-memory records. Malformed and duplicate rows are counted during loading and left in the source file until the user explicitly confirms a save that will replace it.

## Persistence

Records are stored at `data/courses.txt`, relative to the project root. Saving writes a temporary UTF-8 file in the same directory and replaces the saved file only after the complete write succeeds. Each line follows this format:

```text
COURSE_CODE|Course title|credit units
```

Example:

```text
COS 201|Programming I|3
```

Course codes and titles cannot contain the pipe delimiter or line breaks. Saving replaces the previous saved list. Loading validates the complete readable file into a temporary collection before replacing the current in-memory collection; it accepts one row per normalized code and reports malformed/duplicate rows. A missing file is different from an empty file: first launch may initialize samples only for a missing file, while an existing empty file remains empty. The application never automatically saves user changes on exit.

## Project Structure

```text
SCMSMIVA/
|-- data/
|   |-- .gitkeep
|   `-- courses.txt          # Created by first-run initialization or Save
|-- docs/
|   `-- PROJECT_REPORT.md
|-- out/                     # Compiled classes (ignored by Git)
|-- src/
|   |-- AppTheme.java
|   |-- Course.java
|   |-- CourseManager.java
|   |-- Main.java
|   `-- MainFrame.java
|-- .gitignore
`-- README.md
```

## Setup and Launch

Prerequisites: JDK 8 or later and a graphical desktop environment with Swing support. Open a terminal at the project root and check `java -version` and `javac -version`.

Compile all source files and launch:

```sh
mkdir -p out
javac -d out src/*.java
java -cp out Main
```

Run from the project root so `data/courses.txt` resolves to the project’s data directory.

## Testing and Limitations

Compilation with `javac -d out src/*.java` and EDT-driven Swing interaction checks have been performed for first-run sample creation, course addition, duplicate rejection, dashboard metric refresh, normalized search, table filtering, saving, and reload after restarting with a fresh manager. Additional checks covered blank and invalid form values, malformed-row reporting, source-file preservation, canceling and confirming replacement, unreadable-file preservation, failed-save feedback, atomic replacement, empty saves, and missing-file/empty/unsaved states in isolated temporary workspaces. The Java window was launched on the available X11 display and inspected from window-targeted screenshots at 1240×788 and the 1040×690 minimum size.

The sample initialization is deliberately conditional on a missing file. The course form validates and adds in-memory records; users must choose Save to preserve them. The project is a small single-user application and does not provide account management, concurrent editing, or a database.