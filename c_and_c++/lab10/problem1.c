#include <stdio.h>
#include <string.h>

int main(int argc, char **argv) {
    char a[2];

    a[1] = '\0'; //fix by defining null char
    a[0] = 'a';

    if(!strcmp(a, "a")) {
        puts("a is \"a\"");
    }

    return 0;
}
