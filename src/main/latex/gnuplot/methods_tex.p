set terminal latex
set output "methods.tex"
set key off
set yrange [0:0.4]
set ylabel "\\rotatebox{90}{MSE}" 
set boxwidth 0.5
set style fill solid
plot "./data/methods.dat" using 1:3:xtic(2) with boxes,\
    "./data/methods.dat" using 1:($3+0.02):3 with labels