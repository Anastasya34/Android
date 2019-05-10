with authors (author_id)
as (
select author_id
from (select author_id, CONCAT(' ',authorfirstname,' ',authorsecondname,' ',authorsurname) as fio from author) as T
where
(
fio like '% Постников%'
and
fio like '% В%'
and
fio like '% М%'
)
or
(
fio like '% Черненький%'
and
fio like '% В%'
and
fio like '% М%'
)
)
, 
booksT as
 (
select fk_book_id as book_id, count(fk_book_id) as match
from bookauthor
where fk_author_id IN (Select authors.author_id from authors)
group by fk_book_id
)
,
booksTS as (
select book_id, match, RANK 
from booksT, FREETEXTTABLE(book, bookname, 'система') AS KEY_TBL
where book_id = KEY_TBL.[KEY]
)
,
booksTT as(
select themebook.book_id, max(match) as match, sum(booksTS.Rank+KEY_T.RANK) as rank
from booksTS, themebook 
INNER JOIN FREETEXTTABLE(theme, themename, 'методические') AS KEY_T
ON themebook.theme_id = KEY_T.[KEY]
where themebook.book_id = booksTS.book_id
group by themebook.book_id
)
select book.book_id, bookname, fk_dorm, fk_room, fk_board, fk_cupboard
from booksTT, book
where booksTT.book_id = book.book_id
order by Rank DESC, match DESC 
;
