#include <ctype.h>
#include <string.h>
#include "revwords.h"

void reverse_substring(char str[], int start, int end) {
  char c;
  int i, j;
  for (i = start,j = end; i < j ; i++, j--) {
    c = str[i], str[i] = str[j], str[j]=c;
  }
}

int find_next_start(char str[], int len, int i) {
  if (i >= len) {
    return -1;
  }
  int index = -1;
  for (int j = i; j < len; j++) {
    if (isalpha((unsigned char) str[j])) {
      index = j;
      break;
    }
  }
  return index;
}

int find_next_end(char str[], int len, int i) {
  if (i >= len) {
    return -1;
  }
  int index = len;
  for (int j = i + 1; j < len; j++) {
    if (!isalpha((unsigned char) str[j])) {
      index = j;
      break;
    }
  }
  return index;
}

void reverse_words(char s[]) {
  int len = strlen(s);
  int start = find_next_start(s, len, 0);
  int end = find_next_end(s, len, 0);
  while (start >= 0 && end >= 0) {
    reverse_substring(s, start, end - 1);
    start = find_next_start(s, len, end);
    end = find_next_end(s, len, start);
  }
}
