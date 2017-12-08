#include "stdlib.h"
#include "string.h"
#include <stdio.h>
#include <ctype.h>
#include "tokenizer.h"
#define DEFAULTSTRSIZE 10
#define MAXPUNC 4
//this type is used for all dynamically changing collections of chars 
typedef struct {
	char * chars;
	unsigned int pointer;
	unsigned int size;
} stack;

// typedef enum {
// 		KEYWORD, IDENTIFIER, FLOATCONST, INTCONST, CHARCONST,
// 		STRINGLIT, PUCNTUATOR, EOFI, COMMENT, BAD
// } TokenType;
// typedef struct {
// 	TokenType type;
// 	char * tokenText;
// } Token;
stack charStack;
FILE * inputFile;
char * makeString(stack stringStack);
char oneLetterEscapes[11] = {'\"', '\'', '?', '\\', 'a', 'b', 'f', 'n', 'r', 't', 'v'};
const char *keywords[44] = {"_Alignas", "_Atomic", "_Bool",
			"_Complex", "_Generic", "_Imaginary", "_Noreturn", "_Static_assert", "_Thread_local", "alignof", "auto", "break", "case", "char", "const", "continue", "default", "do",
			"double", "else", "enum", "extern", "float", "for", "goto", "if", "inline", "int", "long",
			"register", "restrict", "return", "short", "signed", "sizeof", "static", "struct", "switch",
			"typedef", "union", "unsigned", "void", "volatile", "while"};
const char *puncutuators[54] = {"!", "!=", "#", "##", "%", "%:", "%:%:", "%=", "%>", "&", "&&", "&=", "(", ")", "*", "*=", "+", "++", "+=", ",", "-", "--", "-=", "->", ".", "...", "/", "/=", ":", ":>", ";", "<", "<%", "<:", "<<", "<<=", "<=", "=", "==", ">", ">=", ">>", ">>=", "?", "[", "]", "^", "^=", "{", "|", "|=", "||", "}", "~"};
int getChar();
char getFloatSuffix();
char * getIntSuffix();
int binSearchChar(char* toSearch, char toFind, int lengthOfArray);
int binSearch(const char ** toSearch, char * toFind, int lengthOfArray);
Token getCommentToken(int charAfter);
Token getPunctuatorToken(int firstChar);
Token getCharToken(int firstChar);
Token getStringLitToken(int firstChar);
Token getIntegerOrFloatToken(int nextChar);
Token getIdentifierOrKeywordToken(int nextChar);
Token getStringOrCharTokenText(int firstChar, char singleDouble, TokenType type);
char * escapeSequence();
void push(char charect, stack * charStack);
void ungetChar(char charect);
void printToken(Token token);
char * makeString(stack stringStack)
{
	return (char *)stringStack.chars;
}
void ungetChar(char charect)
{
	push(charect, &charStack);
}
stack initilizeStack(int size)
{
	stack tempStack;
	char * chars = calloc(size, sizeof(char));
	tempStack.chars = chars;
	tempStack.pointer = 0;
	tempStack.size = size;
	return tempStack;
}
char pop(stack * tempStack)
{
	if (tempStack->pointer <= 0)
	{
		return 0;
	}
	return tempStack->chars[--tempStack->pointer];
}
void push(char charect, stack * tempStack)
{
	tempStack->chars[tempStack->pointer] = charect;
	tempStack->pointer++;
	if (tempStack->pointer >= tempStack->size)
	{
		tempStack->size = tempStack->size * 2;
		tempStack->chars = realloc(tempStack->chars, tempStack->size * sizeof(char));
		memset(tempStack->chars + tempStack->pointer * sizeof(char), 0, (tempStack->size - tempStack->pointer) * sizeof(char));

	}
}
void pushStr(char * charectArray, stack * tempStack)
{
	for (int i = 0; charectArray[i] != 0; i++){
		push(charectArray[i], tempStack);
	}
}
Token makeToken(TokenType type, char * tokenText)
{
	Token tempToken;
	tempToken.type = type;
	tempToken.tokenText = tokenText; 
	return tempToken;
}
Token get_Token_private()
{
	int nextChar = getChar(); 
	//printf("yay--%c--\n", nextChar);
	if (nextChar == -1 || nextChar == 0){
		return makeToken(EOFI, "");
	}
	else if (isspace(nextChar)){
		return get_Token_private();
	}
	else if (nextChar == '/'){
		int charAfter = getChar();
		if (charAfter == '*' || charAfter == '/') {
			return getCommentToken(charAfter);
		}
		ungetChar(charAfter);
	}
	else if (nextChar == 'u' || nextChar == 'U' || nextChar == 'L') {
		int charAfter = getChar();
		ungetChar(charAfter);
		if (charAfter == '\'') {
			return getCharToken(nextChar);
		}
		else if (charAfter == '\"') {
			return getStringLitToken(nextChar);
		}
		else if (charAfter == '8'){
			charAfter = getChar();
			int yetAnother = getChar();
			ungetChar(yetAnother);
			ungetChar(charAfter);
				if (yetAnother == '\"') {
				return getStringLitToken(nextChar);
			}
		}
	}
	else if (nextChar == '.'){
		int charAfter = getChar();
		ungetChar(charAfter);
		if (isdigit(charAfter)) {
			return getIntegerOrFloatToken(nextChar);
		}
	}
	if (isalpha(nextChar) || nextChar == '_' || nextChar == '$') {
		return getIdentifierOrKeywordToken(nextChar);
	}
	else if (isdigit(nextChar)) {
		return getIntegerOrFloatToken(nextChar);
	}
	else if (nextChar == '\'') {
		return getCharToken(nextChar);
	}
	else if (nextChar == '\"') {
		return getStringLitToken(nextChar);
	}
	else{
		return getPunctuatorToken(nextChar);
	}

}

Token get_Token()
{
	Token token;
	if (inputFile != 0) {
		token = get_Token_private();
	}
	else{
		return makeToken(BAD, "please get new file");
	}
	if (token.type == BAD) {
		fprintf(stderr, "bad Token aborting file\n");
		inputFile = 0;
		char token_string[1024];
		snprintf(token_string, sizeof(token_string), "bad input: %s", token.tokenText);
		return makeToken(BAD, token_string);
	}
	return token;
}
/**
* gets a puncutuator token,
*/
Token getPunctuatorToken(int firstChar)
{
	// int nextChars[MAXPUNC];
	// // char[] fourChars = new char[MAXPUNC];
	// nextChars[0] = firstChar;
	// for (int i = 1; i < MAXPUNC ; i++) {
	// 	int temp = getChar();
	// 	if (temp != -1) {
	// 		nextChars[i] = temp;
	// 	}
	// 	else{
	// 		nextChars[i] = 0;
	// 	}
	// }
	char * bestPunc;
	//memset(bestPunc, '\0', sizeof(bestPunc));
	stack builder = initilizeStack(DEFAULTSTRSIZE);
	 for (int i = 0; i < MAXPUNC; i++) {
	 	push((char)firstChar, &builder);
	 	if(binSearch(puncutuators, makeString(builder) , 54)){
	 		  bestPunc = strdup(makeString(builder));
	 	}
	 	firstChar = getChar();
	 }
	 ungetChar(firstChar);
	 if (bestPunc == 0){
	 	return makeToken(BAD, (char *)&firstChar);
	 }
	 unsigned int length = strlen(bestPunc);
	 //printf("%u\n", length);
	 for (int i = MAXPUNC - 1; i >= length; i--) {
	 	ungetChar(pop(&builder));
	 }
	 free(builder.chars);
	return makeToken(PUCNTUATOR, bestPunc);
}
Token getIntegerOrFloatToken(int firstChar)
{
		int floting = 0;
		int nextChar = firstChar;
		stack builder = initilizeStack(DEFAULTSTRSIZE);
		int newChar = getChar();
		if (firstChar == '0' && (newChar == 'x' || newChar == 'X')) {
			push('0', &builder);
			push((char)newChar, &builder);
			nextChar = getChar();
			while(isxdigit((char)nextChar)){
				push((char)nextChar, &builder);
				nextChar = getChar();
			}
			ungetChar(nextChar);
			char * suff = calloc(3, sizeof(char));
			getIntSuffix(suff);
			if (suff[0]){
				pushStr(suff, &builder);
			}
			free(suff);
			return makeToken(INTCONST, makeString(builder));
		}
		ungetChar(newChar);
		while(isdigit(nextChar)){
			push((char)nextChar, &builder);
			nextChar = getChar();
		}
		if (nextChar == '.') {
			push((char)nextChar, &builder);
			nextChar = getChar();
			while(isdigit(nextChar)){
				push((char)nextChar, &builder);
				nextChar = getChar();
			}
			floting = 1;
		}
		if (nextChar == 'e' || nextChar == 'E') {
			push((char)nextChar, &builder);
			nextChar = getChar();
			if (nextChar == '+' || nextChar == '-') {
				push((char)nextChar, &builder);
				nextChar = getChar();
			}
			while(isdigit(nextChar)){
				push((char)nextChar, &builder);
				nextChar = getChar();
			}
			floting = 1;
		}
		ungetChar(nextChar);
		if (floting) {
			char suff = getFloatSuffix();
			if (suff != 0){
				push(suff, &builder);
			}
		}
		else{
			char * suff = calloc(3, sizeof(char));
			getIntSuffix(suff);
			if (suff[0]){
				pushStr(suff, &builder);
			}
			free(suff);
		}
		return floting ? makeToken(FLOATCONST, makeString(builder)) : makeToken(INTCONST, makeString(builder));
}
char * getIntSuffix(char * ret)
{
	// int a = getChar();
	// int b = getChar();
	// int c = getChar();
	// if (tolower(a) == 'l' || tolower(a) == 'u'){
	// 		ret[0] = a;
	// 		if (tolower(b) == 'u' && tolower(a) == 'l')
	// 		{
	// 			ret[1] = b;
	// 			return ret;
	// 		}
	// }
	// else{
	// 	return ret;
	// }
	// if ( tolower(b) == 'l')
	// {
	// 	if ((a == b || b == c)){
	// 		if (tolower(c) == 'u' || tolower(a) == 'u'){
	// 			ret[1] = b;
	// 			ret[2] = c;
	// 			return ret;
	// 		}
	// 		else if (tolower(a) == 'l'){
	// 			ret[b] = b;
	// 			return ret;
	// 		}
	// 	}
	// 	ret[1] = b;
	// 	return ret;
	// }
}
char getFloatSuffix()
{
	int nextChar = getChar();
	if (tolower(nextChar) == 'l' || tolower(nextChar) == 'f') {
		return (char)nextChar;
	}
	else{
		ungetChar(nextChar);
		return 0;
	}
}
Token getCommentToken(int mainChar)
{
	stack builder = initilizeStack(DEFAULTSTRSIZE);
	int rollingEnd[2] = {0, 0};
	if (mainChar == '*') {
		push('/', &builder);
		push('*', &builder);
		rollingEnd[0] = '*';
		rollingEnd[1] = '/';
		while(rollingEnd[0] != '/' || rollingEnd[1] != '*'){
			rollingEnd[1] = rollingEnd[0];
			rollingEnd[0] = getChar();
			if (rollingEnd[0] == -1) {
				return makeToken(BAD, makeString(builder));
			}
			push((char)rollingEnd[0], &builder);
		}
		return makeToken(COMMENT, makeString(builder)); 
	}
	else {
		int nextChar = mainChar;
		push(('/'), &builder);
		while(nextChar != '\n'){
			push((char)nextChar, &builder);
			nextChar = getChar();
		}
		ungetChar(nextChar);
		return makeToken(COMMENT, makeString(builder));
	}
}
Token getCharToken(int firstChar)
{
	return getStringOrCharTokenText(firstChar, '\'', CHARCONST);
}
Token getStringLitToken(int firstChar)
{
	return getStringOrCharTokenText(firstChar, '\"', STRINGLIT);
}
Token getIdentifierOrKeywordToken(int firstChar)
{
	int nextChar = firstChar;
	stack builder = initilizeStack(DEFAULTSTRSIZE);
	while(isalnum(nextChar) || nextChar == '_' || nextChar == '$'){
		push((char)nextChar, &builder);
		nextChar = getChar();
	}
	ungetChar(nextChar);
	char * idenString = makeString(builder);
	if (binSearch(keywords,idenString, 44)) {
		return makeToken(KEYWORD, idenString);
	}
	return makeToken(IDENTIFIER, idenString);
}
Token getStringOrCharTokenText(int firstChar, char singleDouble, TokenType type)
{
	stack builder = initilizeStack(DEFAULTSTRSIZE);
	int nextChar = firstChar;
	int qouteCount = 0;
	while(qouteCount < 2){
		if (nextChar == singleDouble) {
			qouteCount++;
		}
		else if (nextChar == '\n') {
			return makeToken(BAD, makeString(builder));
		}
		else if (nextChar == '\\') {
			char * tempString = escapeSequence();
			if (tempString == 0){
				return makeToken(BAD, makeString(builder));
			}
			else{
				push((char)nextChar, &builder);
				pushStr(tempString, &builder);
				free(tempString);
				nextChar = getChar();
				continue;
			}
		}
		push((char)nextChar, &builder);
		nextChar = getChar();
	}
	ungetChar(nextChar);
	return makeToken(type, makeString(builder));
}
char * escapeSequence()
{
	stack escapeReturn = initilizeStack(DEFAULTSTRSIZE);
	int nextChar = getChar();
	if (binSearchChar(oneLetterEscapes, (char) nextChar, 11)){
		push((char) nextChar, &escapeReturn);
		return 	makeString(escapeReturn);
	}
	int newChar = getChar(); 
	if (nextChar == 'u') {
		push((char) nextChar,&escapeReturn);
		for (int i = 0; i < 4; i++) {
			if (!isxdigit((char)newChar)) {
				return 0;
			}
			push((char) newChar,&escapeReturn);
			newChar = getChar();
		}
	}
	else if (nextChar == 'U') {
		push((char) nextChar,&escapeReturn);
		for (int i = 0; i < 8; i++) {
			if (!isxdigit((char)newChar)) {
				return 0;
			}
			push((char) newChar,&escapeReturn);
			newChar = getChar();
		}
	}
	else if (nextChar == 'x') {
		push((char) nextChar,&escapeReturn);
		while (isxdigit((char)newChar)) {
			push((char) newChar,&escapeReturn);
			newChar = getChar();
		}
		ungetChar(newChar);
	}
	else if (48 <= (char)nextChar && (char)nextChar <= 55) {
		int done = 0;
		while (48 <= (char)nextChar && (char)nextChar <= 55 && done < 3) {
			push((char) nextChar, &escapeReturn);
			nextChar = getChar();
			done++;
		}
		ungetChar(nextChar);
	}
	if (escapeReturn.pointer != 0)
	{
		return makeString(escapeReturn);
	}
	return 0;

}
int binSearchChar(char* toSearch, char toFind, int lengthOfArray)
{
	int lo = 0;
	int hi = lengthOfArray - 1;
	int mid;
	while(lo <= hi){
		mid = (lo + hi) / 2;
		if (toSearch[mid] < toFind){
			lo = mid + 1;
		}
		else if (toSearch[mid] > toFind){
			hi = mid - 1;
		}
		else{
			return 1;
		}
	}
	return 0;
}
int binSearch(const char ** toSearch, char * toFind, int lengthOfArray)
{
	int lo = 0;
	int hi = lengthOfArray - 1;
	int mid;
	while(lo <= hi){
		mid = (lo + hi) / 2;
		int less = strcmp(toSearch[mid], toFind);
		if (less < 0){
			lo = mid + 1;
		}
		else if (less > 0){
			hi = mid - 1;
		}
		else{
			return 1;
		}
	}
	return 0;
}
int getChar()
{
	char nextChar = pop(&charStack);
	if (!nextChar){
		nextChar = getc(inputFile);
	}
	return nextChar;
}
void initilize(char * fileName)
{
	inputFile = fopen( fileName, "r" );
	charStack = initilizeStack(10);
	table = calloc(1, sizeof(symbolTable));
	table->rows = calloc(1, sizeof(symbolTableRow));
	table->pointer = 0;
	table->size = 1;
}
void printToken(Token token)
{
	fprintf(stdout,"token type: %d token text:%s\n", token.type, token.tokenText);
}
int main(int argc,char ** argv)
{
	if (argv[1])
	{
		initilize(argv[1]);
	}
	else{
		initilize("./csim.c");
	}
	//push('a', &charStack);
	Token toke = makeToken(KEYWORD, "for");
	while (toke.type != EOFI && toke.type != BAD) {
		toke = get_Token();
		printToken(toke);
		if (toke.type != EOFI && toke.type != BAD && toke.type != PUCNTUATOR)
		{
			free(toke.tokenText);
		}
	}
	if (inputFile)
	{
		fclose(inputFile);
	}
	return 1;
}