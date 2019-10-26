#include <stdlib.h>
#include <stdio.h>
#include "re.h"

arena_t create_arena(int size) {
  arena_t arena = malloc(sizeof(struct arena));
  arena->size = size;
  arena->current = 0;
  arena->elts = malloc(size * sizeof(Regexp));
  return arena;
}

void arena_free(arena_t a) {
  free(a->elts);
  free(a);
}

Regexp *re_alloc(arena_t a) {
  if (a->current < a->size) {
    Regexp *r = a->elts + a->current;
    a->current += 1;
    return r;
  } else {
    return NULL;
  }
}

Regexp *re_chr(arena_t a, char c) {
  Regexp *r = re_alloc(a);
  if (r != NULL) {
    r->type = CHR;
    r->data.chr = c;
    return r;
  } else {
    return NULL;
  }
}

Regexp *re_alt(arena_t a, Regexp *r1, Regexp *r2) {
  Regexp *r = re_alloc(a);
  if (r != NULL) {
    r->type = ALT;
    r->data.pair.fst = r1;
    r->data.pair.snd = r2;
    return r;
  } else {
    return NULL;
  }
}

Regexp *re_seq(arena_t a, Regexp *r1, Regexp *r2) {
  Regexp *r = re_alloc(a);
  if (r != NULL) {
    r->type = SEQ;
    r->data.pair.fst = r1;
    r->data.pair.snd = r2;
    return r;
  } else {
    return NULL;
  }
}

int re_match(Regexp *r, char *s, int i) {
  if (r != NULL) {
    if (r->type == CHR) {
      return s[i] == r->data.chr ? i + 1 : -1;
    } else if (r->type == SEQ) {
      int j = re_match(r->data.pair.fst, s, i);
      if (j < 0) return -1;
      else return re_match(r->data.pair.snd, s, j);
    } else if (r->type == ALT) {
      int j = re_match(r->data.pair.fst, s, i);
      if (j >= 0) return j;
      else return re_match(r->data.pair.snd, s, i);
    }
  }
  return -1;
}



void re_print(Regexp *r) {
  if (r != NULL) {
    switch (r->type) {
    case CHR:
      printf("%c", r->data.chr);
      break;
    case SEQ:
      if (r->data.pair.fst->type == ALT) {
	printf("(");
	re_print(r->data.pair.fst);
	printf(")");
      } else {
	re_print(r->data.pair.fst);
      }
      if (r->data.pair.snd->type == ALT) {
	printf("(");
	re_print(r->data.pair.snd);
	printf(")");
      } else {
	re_print(r->data.pair.snd);
      }
      break;
    case ALT:
      re_print(r->data.pair.fst);
      printf("+");
      re_print(r->data.pair.snd);
      break;
    }
  } else {
    printf("NULL");
  }
}
