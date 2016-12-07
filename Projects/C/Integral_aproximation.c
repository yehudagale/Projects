#include <stdio.h>
#include <stdlib.h>
#include <math.h>
double f(double (x));
double rect (double (*f)(double), double start, double end, int times);
double trap (double (*f)(double), double start, double end, int times);
double simp (double (*f)(double),double start, double end, int times);

//calculates and prints aproximations of the integral of the function defined in f

int main(int argc, char const *argv[])
{
	double start, end;
	int times;
	printf("What is start?\n");
  	scanf("%lf", &start);
  	printf("What is end?\n");
  	scanf("%lf", &end);
	printf("How many peices should we divide the integral into?\n");
  	scanf("%d", &times);
	double print = rect(f, start, end, times);
	printf("rect: %lf\n", print);
	print = trap(f, start, end, times);
	printf("trap: %lf\n", print);
	print = simp(f, start, end, times);
	printf("simp: %lf\n", print);
	return 0;
}
//uses the rectagular aproximation
double rect (double (*f)(double), double start, double end, int times)
{
	int i; //index variable
	double total = 0; //total for the for loop
	double factor = (end - start) / (2 * times);
	for (i = 1; i <= times; ++i)
	 {
	 	total += f(start + (2 * i - 1) * factor);
	 }
	 return total * ((end - start) / times);
}
//uses the trapazoidal aproximation
double trap (double (*f)(double), double start, double end, int times)
{
	int i; //index variable
	double total = 0; //total for the for loop
	double factor = (end - start) / times;
	for (i = 1; i < times; ++i)
	 {
	 	total += f(start + i * factor);
	 }
	 total += (f(start) + f(end)) * 0.5;
	 return total * ((end - start) / times);
}
//uses simpson's aproximation
double simp (double (*f)(double),double start, double end, int times)
{
	return (2 * rect(f, start, end, times) + trap(f, start, end, times)) / 3;
}
//the function to be aproximated
double f(double (x))
{
	return 2 * x;
}