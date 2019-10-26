#include <stdlib.h>
#include <stdio.h>
#include <stdint.h>

typedef struct {
  int a,b,c;
} st;

int main(void) { 
  st obj = {10,12,15};
  st *p1 = &obj;
  st *p2 = NULL;
  intptr_t n = (intptr_t) &p2->b; // why doesn't this trigger a segmentation fault?

  printf("%ld\n", *(intptr_t *) ((char*)p1 + n));
  return 0;
}
