;; The first three lines of this file were inserted by DrRacket. They record metadata
;; about the language level of this file in a form that our tools can easily process.
#reader(lib "htdp-intermediate-lambda-reader.ss" "lang")((modname robot-v2) (read-case-sensitive #t) (teachpacks ()) (htdp-settings #(#t constructor repeating-decimal #f #t none #f ())))
;;Austin Olin and Miles Gillis
;;PS08

(require rackunit)
(require rackunit/text-ui)
;(require racket/list)
(require racket/base)
(require "extras.rkt")
(require "obstacles.rkt")
(require "sets.rkt")
(provide path)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;DATA DEFINITIONS

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; A Position is a (list PosInt PosInt)
;; (x y) represents the position at position x, y.
;; Note: this is not to be confused with the built-in data type Posn.

;; A Move is a (list Direction PosInt)
;; Interp: a move of the specified number of steps in the indicated
;; direction. 

;; A Direction is one of
;; -- "north"
;; -- "east"
;; -- "south"
;; -- "west"

;; A Plan is a ListOf<Move>
;; WHERE: the list does not contain two consecutive moves in the same
;; direction. 

;; A Maybe<Plan> is one of
;; - false
;; - Plan


;; A Chain is a Listof<X>, one of...
;; - empty
;; - (cons Position Listof<Position>)               
;; - (cons false (cons Position Listof<Position>))
;; WHERE: Positions that are adjacent in the list are also adjacent on the board
;;  and no two Positions are the same
;; INTERPRETATION: A Chain that is headed by false is a dead end that will not 
;;  have any additional positions added to it. 


;; A MaybeChain is one of 
;; - false
;; - (cons Position Listof<Position>)
;; WHERE: Positions that are adjacent in the list are also adjacent on the board
;;  and no two Positions are the same
;; INTERPRETATION: A MaybeChain is false if there is no sequence of adjacent 
;;  Positions from a start Position to an end Position, or that sequence of 
;;  Positions if there is one.


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;CONSTANTS

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;




;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;FUNCTION DEFINITIONS

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


;path : Position Position ListOf<Position> -> Maybe<Plan>
;GIVEN:
;1. the starting position of the robot,
;2. the target position that robot is supposed to reach
;3. A list of the blocks on the board
;RETURNS: a plan that, when executed, will take the robot from
;the starting position to the target position without passing over any
;of the blocks, or false if no such sequence of moves exists.
;;STRAT, HALTING, etc.. :

(define (path start end blocks)
  (cond
    [(my-member? end blocks) false]
    [(equal? start end) empty]
    ;;A case when start is in blocks?
    [else (chain-to-path (make-chains (list blocks (list start)) end))]))

;(define b1awksX (list (list 2 1)(list 2 2)(list 2 3)))
;(define bncex (list b1awksX (list (list 1 1) )  ))
;(define end (list 3 1))
;()
;(list (list (list 2 1)(list 2 2)(list 2 3)) (list (list 1 1))) (list 3 1)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;chain-to-path: MaybeChain -> Maybe<Plan>
;;GIVEN
;;RETURN
;;...

(define (chain-to-path mb-chain)
  mb-chain)


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;make-chains: Listof<Chain> Position -> MaybeChain
;;GIVEN: A List of PositionSets, the first of which is the set of all occupied
;; and visited Positions, and a Position we are trying to reach
;;WHERE: The PositionSetSet always has at least 2 elements, the second of which
;; is never empty
;;RETURN: A MaybeChain indicating a sequence of Positions leading to the end 
;; Position, or false if no such sequence exists.

(define (make-chains blocks-and-chains end)
  (let* ([blocks (first blocks-and-chains)]
         [chains (rest blocks-and-chains)]
         [updated-bnc
          (foldr (λ (one-chain chains-so-far)
                   (if (false? (first one-chain))
                       (append chains-so-far (list one-chain))
                       ;;else, important part: Update visited Positions and add
                       ;; adjacent, unvisited Positions to the start of each 
                       ;; chain 
                       (let ([visited (find-visited chains-so-far)])
                         (append chains-so-far (add-links one-chain visited)))))
                 (list blocks)
                 chains)])
    (check-chains updated-bnc end)))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;find-visited: Listof<Chain> -> PositionSet/Listof<Position>
;;RETURNS: The union of the Positions in each Chain

(define (find-visited blocks-and-chains)
  (let ([blocks (first blocks-and-chains)]
        [chains (rest blocks-and-chains)])
    (foldr (λ (one-chain visited)
             (if (false? (first one-chain))
                 (set-union (rest one-chain) visited)
                 (set-union one-chain visited)))
           blocks
           chains))) 

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;add-links: Chain PositionSet -> Listof<Chain>
;;GIVEN: A Chain to add links to and the set of Positions to exclude (those that
;; have already been visited or are occupied)
;;WHERE: The given Chain is not a dead end
;;RETURN: A List of Chains, each consisting of an unvisited, adjacent Position
;; added to the given Chain, or a single element list of the given Chain with
;; false added to its head if there are no possible moves to make

(define (add-links chain visited)
  (let ([links (set-diff (find-adjacent-positions (first chain)) visited)])
    (if (empty? links)
        (list (cons false chain))
        (map (λ (one-link) (cons one-link chain))
             links))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;check-chains: Listof<Chain> Position -> MaybeChain

(define (check-chains blocks-and-chains end)
  (let* ([chains (rest blocks-and-chains)]
         [candidates (filter (λ (chain) (not (false? (first chain)))) chains)]
         [found-it (filter (λ (chain) (equal? (first chain) end)) candidates)])
    (cond
      [(empty? candidates) false]
      [(empty? found-it) (make-chains blocks-and-chains end)]
      [else (first found-it)])))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(make-chains (list (list (list 2 1)(list 2 2)(list 2 3)) (list (list 1 1))) (list 3 1))