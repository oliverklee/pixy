# Pixy


## Further documentation

Documentation on this program can be found at <http://pixybox.seclab.tuwien.ac.at/>.


## Developing Pixy in Eclipse

The most recent version of Eclipse can be obtained from <http://www.eclipse.org/>.


### Importing, adjusting & initial build

1. create a new Java project with the default settings (name: Pixy)
  (--> the "eclipsed" project will reside inside the Eclipse workspace
  after you've finished the following steps; if you want to have it somewhere
  else, export it, delete it, and import it again; see below)

2. disable "build automatically" (from the project menu)

3. from the newly created project's context menu (package explorer), select "Import"
  - File System
    - choose the Pixy home folder (i.e. double click it, then press OK)
    - select it on the following screen (to import everything inside)
      ("create selected folders only"), finish

4. if a red X appears next to the icon of the Pixy project, you forgot to disable
  "build automatically"

5. adjust project properties (from its context menu again):
  - Builders:
    - deselect "Java Builder"
  - Java Build Path:
    - Source Tab:
      - remove existing folder (Pixy)
      - add new folders:
        - Pixy/src/project
        - Pixy/src/java_cup (UPDATE: not necessary any more)
        - Pixy/src/JFlex (UPDATE: not necessary any more)
        - [after the first successful build:]
          Pixy/build/java
      - set default output folder to Pixy/build/class
    - Libraries Tab:
      - Add Class Folder:
        - Pixy/lib
      - Add Jar:
        - Pixy/lib/junit.jar

6. context menu of build.xml: Run / 2.Ant Build...
  - name it "Pixy build"
  - main tab:
    - Base directory = Pixy folder
  - refresh tab:
    - select "refresh upon completion", "project containing the selected resource"
  - targets tab:
    - select "build" target instead of preselected default target
  - run! (should be successful)


### Running

1. Run / Run...
  - Java Application, New
  - Main tab:
    - name = Pixy default
    - Main Class: should find Checker after pressing the Search button, accept it
  - Arguments tab:
    - Program arguments:
      ${project_loc}/test.php
    - VM Arguments:
      -Dpixy.home=${project_loc}
  - run!
  - for JUnit Tests: same VM Argument


### Development building

1. context menu of build.xml: Run / External tools
  - duplicate existing build configuration
  - rename the copy to "Pixy javac"
  - replace target "build" with "javac"
  - run


### Exporting

1. don't forget to clean up first
2. as zipfile or to file system
3. check if it contains a .project file (that's how it should be, otherwise
  you can't import it back to Eclipse easily)

### Importing again
1. unzip file outside eclipse (if zipped); that location is not just temporary,
  but will be used by eclipse as project home
2. don't create a project
3. context menu of package explorer: Import... Existing project into workspace,
  select unzipped home folder, finish

## Usage (Eclipse)

Thanks to Chuck Burgess (cburgess at PROGRESSRAIL dot com) for
contributing these instructions.

To set up Pixy as an "External Tool" to use inside Eclipse (tested on 
v3.1.2), perform these steps:

1. From the "Run" menu, choose "External Tools", then "External Tools" 
  again.  This brings up the dialogue where you can edit/create 
  "External Tools" items.
2. On the left-side menu, highlight the "Programs" heading and click 
  the "New" button.
3. Populate the "Name" field on the main top-center area with whatever 
  name you want to use to recognize this program for yourself (i.e. 
  "Pixy Vulnerability Scanner for PHP").
4. On the "Main" tab, I populated these items in this manner:
     - Location: 			${workspace_loc}/Pixy/run.bat 
     - Working Directory:  	${workspace_loc}${project_path}
     - Arguments:  		"${workspace_loc}${resource_path}"
  Notice the quotes in the "Arguments" value... they are needed here, 
  though not allowed in the other values.  If you physically put the 
  Pixy code in your Eclipse workspace, you can use the ${workspace_loc} 
  variable in the path to the "run" or "run.bat" file in the "Location" 
  item, rather than having to hardcode the path.  Otherwise, you should 
  hardcode the path in the "Location" item, but not in the "Working 
  Directory" or "Arguments" items...  (these items refer to your workspace 
  area/items specifically).
5. No changes should be necessary on the "Refresh", "Environment", or 
  "Common" tabs.

To run Pixy on a workspace PHP file, just highlight the file (either on 
the left-side Navigator menu listing, or else the opened file's tab in 
the center view area).  Click "Run -> External Tools", and choose your 
Pixy item's name.  The output from its run will appear in the "Console" 
view at the bottom of the screen layout.  Remember, Pixy runs against 
the file you have HIGHLIGHTED, which is not necessarily the one you 
have OPEN.


## Modifications to JFlex 1.4.1
- marked with "NJ"
- modified files:
  - skeleton.default
  - Emitter.java
- reasons:
  - emulation of Flex's yymore()
  - in case of error: also include file name and line number in the output message
- state stacks are emulated without modifying the JFlex sources, simply by
  including the relevant data structures into the specification file

## Modifications to Cup 10k
- marked with "NJ"
- modified files:
  - emit.java
- reasons:
  - indices for terminals and nonterminals in the generated symbols class shall not overlap
  - nonterminals in the generated symbols class shall be public
  - rule actions shall have access to
    - the production's symbol index (i.e. the index of the symbol on the left side)
    - the production's symbol name
    - the production's length
   since that makes the generation of explicit parse trees much easier

## Licenses
* jFlex: GPL
* Cup: custom license, derived from "Standard ML of New Jersey", GPL-compatible
* CLI: Apache license
* rest: GPL