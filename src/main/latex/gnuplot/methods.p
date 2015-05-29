set terminal latex
#set terminal pngcairo size 800,600 dashed enhanced font 'Verdana,10'
set output "methods.tex"
#set output "methods.png"
set key off
set yrange [0:0.35]
set boxwidth 0.5
set style fill solid
plot "method.dat" using 1:3:xtic(2) with boxes