# Automated Gaussian Scan Analyzer

Developed Java program to read output files from Gaussian coordinate scans and return input files for later calculations.

Gaussian is a computational chemistry software that can calculate single-point energy, optimized geometries, transistion states, and other chemical properties based on an input molecular geometry. 

A Gaussian coordinate scan changes one feature of a molecule's geometry for a certain amount of steps. At each step, the rest of the molecule's geometry is re-optimized (for lowest energy) after the initial change. This program extracts each re-optimized geometry from a Gaussian coordinate scan and creates new input files using those geometries for future calculations. This streamlines the process of analyzing coordinate scan results.  
