----------------------
- JAdvanScene ReadMe -
----------------------

1. Introduction

JAdvanScene (jas) is a JAVA commandline tool to manage ROMs files according to 
datafiles available on http://www.advanscene. It scans files in a folder and 
rename them and compress them as specified by the datafile.
This tool has been developed in the spirit of KISS (keep it simple) as it just 
does its job.

2. Installation

Untar the distribution where you want and execute in a terminal :
java -jar <path.to.jas>/jas.jar
It will create a properties file named .jas/jas.properties in your home folder.
You then have to indicate the two following properties :
<system>.dat=<path.to.zipped.datafile> : the path to the zip containing a 
datafile for the <system>.
<system>.path=<path.to.roms> : the folder containing the ROMs files.

Here is a sample from my jas.properties :
nds.dat=/home/benoit/.jas/ADVANsCEne_NDS_S.zip
nds.path=/home/benoit/jeux/Nintendo DS

Note that you may have an infinite number of <system>.dat/path couple.

3. Usage

Now that jas is properly configured you may run the following command :
java -jar <path.to.jas>/jas.jar <system>
where <system> is one of the system you specified in jas.properties.

JAdvanScene will rename and zip files having crc corresponding in datafile.
It will delete unknown files (nfo, jpg, diz, ...) and will keep uncompressed 
files having a known extension for the system (for examples *.nds and *.bin 
will be kept for Nintendo DS and *.gba, *.agb and *.bin will be kept for 
Nintendo GBA).  

4. Properties

There are some properties in jas.property that we didn't talk previously.
- proxy : perhaps are you using a proxy, in this case fill this property.
          Note that the default value (user\:password@host\:port) is NOT 
          considered as proxy settings.
- test (yes/no) : use this if you don't trust jas and just want to see what 
                  jas will do. In test mode the files are NOT touched. Note 
                  that I've used jas for the past year without a problem.
- locations : used to determine how to convert datafile location to text.
              Normally you don't have to touch this a new language may be 
              added to datafile.
- languages : used to determine how to convert datafile language to text.
              As I've used retro-engineering to determine these values some 
              can be missing or wrong.

5. FAQ

What is the licence of jas ?
Jas is licenced under GPL, you can use it freely :)

Do you plan new releases ?
As said jas just does its job and it does well (sounds like an ad ;) but I may 
release newer versions mainly for bugfixes.

May I request improvements ?
You can request improvements but I want to keep jas slim. It fits my needs :
I put my ROMs in a folder, execute it and it rename and zip the ROMs, that's 
all I need.

So why jas ?
When I begin to collect some NDS ROMs I didn't find any good renamer for Linux.
So I wrote one in Python (to learn this language too) but it was too slow, 
buggy and unmaintable (well it was my first python project). As I know better 
JAVA I decided to rewrite it in JAVA and so here comes jas.

May I help jas ?
You're welcome if you want to contribute to jas. Just mail me and we'll see.
There are a lot of areas that may be improved : translations, logging, ...

May I fork jas ?
If you want to improve a lot jas and so feel the need to fork no problem. As 
said I don't plan to implement a lot a features in jas so I'll be pleased that 
jas is used as a base. Please just contact me to tell me you're forking jas.

6. ToDos
Manage proxies.
Website.
Official Release.
Complete languages.
Rename jas.jar.
Rename zip file even if containing file is well named.
Remove nanoxml dependancy.

7. Contact

Website : ?
Mail    : bgiraudou@gmail.com
