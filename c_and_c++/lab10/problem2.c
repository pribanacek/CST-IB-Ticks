#include <stdio.h>

void foo(int i) {
  //loop can disappear
  while (i) {
    /* loop? */
  }
}

int main(void) {
  foo(1);
  printf("Done!?\n");
  return 0;
}
