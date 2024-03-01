# Homstrad

## Strukturelle Alignments
Indem nicht nur die Sequenzen, sondern auch die Struktur der 
Proteine betrachtet werden, können die Ähnlichkeit von Proteinen besser bestimmt werden. 
Dadurch können auch Proteine, die in der Sequenz sehr unterschiedlich sind, aber in der
Struktur sehr ähnlich, als ähnlich erkannt werden.

Strukturelle alignments können z.B. durch räumliche Überlagerung der Strukturen erzeugt werden.
Vor allem sind hier die Sekundärstrukturen von Bedeutung aber auch die 
räumliche Anordnung dieser. 
Für strukturelle Alignments muss natürlich die Struktur der Proteine bekannt sein,
oder zumindest eine gute Vorhersage der Struktur vorliegen.

Strukturelle Alignments können zur Validierung von Sequenzalignments verwendet werden,
da sie die Ähnlichkeit von Proteinen besser bestimmen können.
Form follows function: Proteine mit ähnlicher Funktion haben oft auch eine ähnliche Struktur.



Hallo hier ist Malte von Gruppe 3:

Aufgabe 14:
Wie gehe ich mit den domains um?
es gibt zb die superfamily psaA_psaB indem zwei proteine mit einander aligned worden sind,
jedoch ict die pdb id gleich: 1jb0, nur dass eimal die domäne a: 1jb0a und die domäne b: 1jb0b gegeben ist.

beim einlesen berücksichtige ich diese domäne, aber wenn wir diese proteine nun in die DB einfügen wollen, können wird die domäne doch nicht dran lassen, weil weder 1jb0a noch 1jb0b eine valide pdb id sind
NEU

Tobias — heute um 18:19 Uhr
du könntest z.B. in die relation eine spalte für die domains anlegen
[18:22]
vielleicht reicht es dann aber auch mit einem eintrag, müsste aber ein @Betreuer bestätigen

Evi — heute um 18:25 Uhr
Ich würde es einfach nicht als Sequenz von der pdb id speichern (falls es um die sequence Tabelle geht) sondern da als domäne. Bei der homstrad Tabelle kannst du auch direkt die pdb id nehmen weil es zusammen mit der Family wieder eindeutig sein sollte
