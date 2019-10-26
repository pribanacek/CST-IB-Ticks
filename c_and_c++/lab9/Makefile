FLAGS=-g -Wall -pedantic 

all:
	gcc ${FLAGS} -Wall -pedantic -c lab9.c
	gcc ${FLAGS} -Wall -pedantic -c parse.c
	gcc ${FLAGS} -Wall -pedantic -c expr.c
	gcc ${FLAGS} -o lab9 lab9.o parse.o expr.o 

clean:
	rm -f lab9 lab9.o parse.o expr.o 
