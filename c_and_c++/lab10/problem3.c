/* Find different optimization levels which will change the
   behaviour of the program. */

//O2 and O3 probs implement tail recursion on this
//seg fault is max recursion depth
void foo(int i) {
  foo(i+1);
}


int main(void) {
  foo(0);
  return 0;
}
