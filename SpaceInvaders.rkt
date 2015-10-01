;; The first three lines of this file were inserted by DrRacket. They record metadata
;; about the language level of this file in a form that our tools can easily process.
#reader(lib "htdp-intermediate-reader.ss" "lang")((modname assignment8si) (read-case-sensitive #t) (teachpacks ((lib "image.rkt" "teachpack" "2htdp") (lib "guess.rkt" "teachpack" "htdp"))) (htdp-settings #(#t constructor repeating-decimal #f #t none #f ((lib "image.rkt" "teachpack" "2htdp") (lib "guess.rkt" "teachpack" "htdp")))))
;; CS2500 
;; Assignment 8
;; Austin Olin (aeolin13@gmail.com)


;;===================================================
;;===================================================

;; DATA DEFINITIONS

(require 2htdp/image)
(require 2htdp/universe)

;; An Invader is a Posn 

;; A Bullet is a Posn 

;; A Location is a Posn 

;; A Direction is one of: 
;; - 'left 
;; - 'right 

;; A Ship is (make-ship Direction Location) where 
;; dir is the current direction of movement 
;; loc is the coordinates of the ship's current location 
(define-struct ship (dir loc))


;; A World is (make-world Ship Lof[Invader] Lof[Bullet]) Lof[Bullet]
;; represent the ship, the current list of invaders, the inflight ship bullets
(define-struct world (ship invaders ship-bullets invader-bullets))

;;================================================================
;;================================================================

;; CONSTANTS

(define WIDTH 500) 
(define HEIGHT 500) 

(define MAX-SHIP-BULLETS 3)

(define MAX-INVADER-BULLETS 10)

(define BACKGROUND (empty-scene WIDTH HEIGHT))

(define SPACESHIP-BULLET-IMAGE (circle 2 'solid 'black))

(define SHIP-WIDTH 25)

(define SHIP-HEIGHT 15)

(define SPACESHIP-IMAGE (rectangle SHIP-WIDTH SHIP-HEIGHT 'solid 'black))

(define INVADER-SIDE 20)

(define INVADER-IMAGE (square INVADER-SIDE 'solid 'red))

(define INVADER-BULLET-IMAGE (circle 2 'solid 'red))

(define SHIP-SPEED 10)

(define BULLET-SPEED 10)

(define SHIP-INIT (make-ship 'left (make-posn 250 480)))

(define INVADERS-INIT (list (make-posn 100 20) (make-posn 140 20) (make-posn 180 20) (make-posn 220 20) (make-posn 260 20)
                            (make-posn 300 20) (make-posn 340 20) (make-posn 380 20) (make-posn 420 20)
                            (make-posn 100 50) (make-posn 140 50) (make-posn 180 50) (make-posn 220 50) (make-posn 260 50)
                            (make-posn 300 50) (make-posn 340 50) (make-posn 380 50) (make-posn 420 50)
                            (make-posn 100 80) (make-posn 140 80) (make-posn 180 80) (make-posn 220 80) (make-posn 260 80)
                            (make-posn 300 80) (make-posn 340 80) (make-posn 380 80) (make-posn 420 80)
                            (make-posn 100 110) (make-posn 140 110) (make-posn 180 110) (make-posn 220 110) (make-posn 260 110)
                            (make-posn 300 110) (make-posn 340 110) (make-posn 380 110) (make-posn 420 110)))

(define WORLD-INIT (make-world SHIP-INIT INVADERS-INIT empty empty))

;;===============================================================================
;;===============================================================================

;; DRAWING

;; world-draw : World -> Image 
;; Given a world, will draw the world on the canvas

(define (world-draw w)
  (draw-bullets (world-invader-bullets w) 
                'invader 
                (draw-bullets (world-ship-bullets w) 
                              'ship 
                              (draw-ship (world-ship w)
                                         (draw-invaders (world-invaders w) 
                                                        BACKGROUND)))))
;; Tests
(check-expect (world-draw (make-world SHIP-INIT empty empty empty))
              (place-image SPACESHIP-IMAGE
                           250
                           480
                           BACKGROUND))


;; draw-ship : Ship Image -> Image
;; Given a ship and a background image, it will draw the
;; ship on to the image and return the new image

(define (draw-ship s img)
  (place-image SPACESHIP-IMAGE
               (posn-x (ship-loc s)) 
               (posn-y (ship-loc s))
               img))

;; Tests
(check-expect (draw-ship SHIP-INIT BACKGROUND) (place-image SPACESHIP-IMAGE
                                                            250 
                                                            480
                                                            BACKGROUND))



;; draw-invaders : Lof[Invader] Image -> Image
;; Given a list of posns representing invaders and a background
;; image, will draw the invaders on the image and return it

(define (draw-invaders loi img)
  (cond
    [(empty? loi) img]
    [else (place-image INVADER-IMAGE
                       (posn-x (first loi))
                       (posn-y (first loi))
                       (draw-invaders (rest loi) img))]))

;; Tests
(check-expect (draw-invaders empty BACKGROUND) BACKGROUND)
(check-expect (draw-invaders (list (make-posn 10 10) (make-posn 100 100)) BACKGROUND)
              (place-image INVADER-IMAGE
                           10
                           10
                           (place-image INVADER-IMAGE
                                        100
                                        100
                                        BACKGROUND)))

;; draw-bullets : Lof[Bullet] Symbol Image -> Image
;; Given a list of either ship bullets or invader bullets, a 
;; background image, and a symbol indicating what type of bullet list
;; ('ship or 'invader), will draw the bullets on the image and return it

(define (draw-bullets lob s img) 
  (cond
    [(empty? lob) img]
    [ else (place-image (bullet-type-image s)
                        (posn-x (first lob))
                        (posn-y (first lob))
                        (draw-bullets (rest lob) s img))]))

;; Tests
(check-expect (draw-bullets empty 'ship BACKGROUND) BACKGROUND)
(check-expect (draw-bullets (list (make-posn 10 10) (make-posn 100 100)) 'ship BACKGROUND)
              (place-image SPACESHIP-BULLET-IMAGE
                           10
                           10
                           (place-image SPACESHIP-BULLET-IMAGE
                                        100
                                        100
                                        BACKGROUND)))

;;=========================================================================
;;=========================================================================


;; INTERACTIONS

;; world-step: World -> World 
;; Given the current world generate the next one and return it 

(define (world-step w) 
  (remove-hits-and-out-of-bounds (make-world (move-ship (world-ship w))
                                             (world-invaders w)
                                             (map move-spaceship-bullets (world-ship-bullets w))
                                             (map move-invader-bullets (invaders-fire (world-invader-bullets w) 
                                                                                      (world-invaders w))))))

;; Tests
(check-expect (world-step (make-world
                           SHIP-INIT
                           INVADERS-INIT
                           empty
                           (list (make-posn 1 1)
                                 (make-posn 2 2)
                                 (make-posn 3 3)
                                 (make-posn 4 4)
                                 (make-posn 5 5)
                                 (make-posn 6 6)
                                 (make-posn 7 7)
                                 (make-posn 8 8)
                                 (make-posn 9 9)
                                 (make-posn 10 10))))
              (make-world
               (make-ship 'left (make-posn 240 480))
               INVADERS-INIT
               empty
               (list (make-posn 1 11)
                     (make-posn 2 12)
                     (make-posn 3 13)
                     (make-posn 4 14)
                     (make-posn 5 15)
                     (make-posn 6 16)
                     (make-posn 7 17)
                     (make-posn 8 18)
                     (make-posn 9 19)
                     (make-posn 10 20))))

;; key-handler : World Key-Event -> World
;; Handle things when the user hits a key on the keyboard.
(define (key-handler w ke)
  (cond 
    [(or (key=? ke "left")
         (key=? ke "right"))
     (make-world (make-ship (string->symbol ke) (ship-loc (world-ship w)))
                 (world-invaders w)
                 (world-ship-bullets w)
                 (world-invader-bullets w))]
    [(key=? ke " ") (ship-fire w)]
    [else w])) 

;; Tests

(check-expect (key-handler WORLD-INIT "up") 
              WORLD-INIT)
(check-expect (key-handler WORLD-INIT "right") 
              (make-world 
               (make-ship 'right (make-posn 250 480))
               INVADERS-INIT
               empty 
               empty))
(check-expect (key-handler WORLD-INIT " ") 
              (make-world 
               SHIP-INIT
               INVADERS-INIT
               (list (make-posn 250 480)) 
               empty))




;; ship-fire : World -> World
;; Given a World, will create a ship-bullet where the spaceship
;; is located, and return the new world

(define (ship-fire w)
  (cond
    [(< (length (world-ship-bullets w)) MAX-SHIP-BULLETS) 
     (make-world (world-ship w)
                 (world-invaders w)
                 (cons (ship-loc (world-ship w))
                       (world-ship-bullets w))
                 (world-invader-bullets w))]
    [else w]))

;; Tests
(check-expect (ship-fire (make-world
                          SHIP-INIT
                          (list (make-posn 100 100))
                          empty
                          empty))
              (make-world
               SHIP-INIT
               (list (make-posn 100 100))
               (list (make-posn 250 480))
               empty))
(check-expect (ship-fire (make-world
                          SHIP-INIT
                          (list (make-posn 100 100))
                          (list (make-posn 10 10)
                                (make-posn 20 20)
                                (make-posn 30 30))
                          empty))
              (make-world
               SHIP-INIT
               (list (make-posn 100 100))
               (list (make-posn 10 10)
                     (make-posn 20 20)
                     (make-posn 30 30))
               empty))

;; bullet-type-image : Symbol -> Image
;; Given a symbol indicating which type of bullet list ('ship or 'invader),
;; will return the correct image for that type of bullet

(define (bullet-type-image s)
  (cond
    [(symbol=? s 'ship) SPACESHIP-BULLET-IMAGE]
    [else INVADER-BULLET-IMAGE]))

;; Tests
(check-expect (bullet-type-image 'ship) SPACESHIP-BULLET-IMAGE)
(check-expect (bullet-type-image 'invader) INVADER-BULLET-IMAGE)

;; move-ship: Ship -> Ship
;; Given a ship, it will move it in the appropriate direction 

(define (move-ship s)
  (make-ship (ship-dir s)
             (cond
               [(symbol=? (ship-dir s) 'left) 
                (cond
                  [(<= (- (posn-x (ship-loc s)) SHIP-SPEED) 
                       (/ SHIP-WIDTH 2))
                   (make-posn (/ SHIP-WIDTH 2) 480)]                                                
                  [else (make-posn 
                         (- (posn-x (ship-loc s)) SHIP-SPEED)
                         (posn-y (ship-loc s)))])]
               [else (cond
                       [(>= (+ (posn-x (ship-loc s)) SHIP-SPEED) 
                            (- WIDTH (/ SHIP-WIDTH 2)))
                        (make-posn (- WIDTH (/ SHIP-WIDTH 2)) 480)]
                       [else (make-posn 
                              (+ (posn-x (ship-loc s)) SHIP-SPEED)
                              (posn-y (ship-loc s)))])])))

;; Tests
(check-expect (move-ship SHIP-INIT) (make-ship 'left (make-posn 240 480)))
(check-expect (move-ship (make-ship 'left (make-posn 8 480))) (make-ship 'left (make-posn (/ SHIP-WIDTH 2) 480)))
(check-expect (move-ship (make-ship 'right (make-posn 250 480))) (make-ship 'right (make-posn 260 480)))
(check-expect (move-ship (make-ship 'right (make-posn 492 480))) (make-ship 'right (make-posn (- 500 (/ SHIP-WIDTH 2)) 480)))

;; move-spaceship-bullets : Bullet -> Bullet
;; Given a spaceship bullet, it will move the spaceship bullet 
;; in the list upwards by BULLET-SPEED units 

(define (move-spaceship-bullets b)
  (make-posn (posn-x b) (- (posn-y b) BULLET-SPEED)))

;; Tests
(check-expect (move-spaceship-bullets (make-posn 100 100)) (make-posn 100 90))


;; move-invader-bullets : Bullet -> Bullet
;; Given an invader bullet, it will move the bullet 
;; downwards by BULLET-SPEED units 

(define (move-invader-bullets b)
  (make-posn (posn-x b) (+ (posn-y b) BULLET-SPEED)))

;; Tests
(check-expect (move-invader-bullets (make-posn 100 100)) (make-posn 100 110))


;; invaders-fire : Lof[Bullet] Lof[Invader]-> Lof[Bullet] 
;; Given a list of invader bullets and a list of invaders, it will 
;; fire from a random invader to hit the ship 

(define (invaders-fire lob loi)
  (cond
    [(empty? loi) empty]
    [(< (length lob) MAX-INVADER-BULLETS) (cons (list-ref loi (random (length loi))) lob)]
    [else lob]))

;; Tests
(check-expect (invaders-fire empty empty) empty)
(check-expect (invaders-fire empty (list (make-posn 1 1))) (list (make-posn 1 1)))
(check-expect (invaders-fire (list (make-posn 1 1)
                                   (make-posn 2 2)
                                   (make-posn 3 3)
                                   (make-posn 4 4)
                                   (make-posn 5 5)
                                   (make-posn 6 6)
                                   (make-posn 7 7)
                                   (make-posn 8 8)
                                   (make-posn 9 9)
                                   (make-posn 10 10))
                             (list (make-posn 1 1)))
              (list (make-posn 1 1)
                    (make-posn 2 2)
                    (make-posn 3 3)
                    (make-posn 4 4)
                    (make-posn 5 5)
                    (make-posn 6 6)
                    (make-posn 7 7)
                    (make-posn 8 8)
                    (make-posn 9 9)
                    (make-posn 10 10)))




;; remove-hits-and-out-of-bounds: World -> World 
;; Given a world, it remove any invaders that have been hit by 
;; a spaceship bullet. Remove any bullets that are out of bounds or have
;; hit an invader.
(define (remove-hits-and-out-of-bounds w)
  (make-world (world-ship w) 
              (remove-hits-generic (world-ship-bullets w) (world-invaders w))
              (remove-hits-generic (world-invaders w) (filter bullet-in-bounds? (world-ship-bullets w)))
              (filter bullet-in-bounds? (world-invader-bullets w))))

;; Tests
(check-expect (remove-hits-and-out-of-bounds (make-world
                                              SHIP-INIT
                                              (list (make-posn 10 10))
                                              (list (make-posn 10 10))
                                              (list (make-posn 600 600))))
              (make-world
               SHIP-INIT
               empty
               empty
               empty))


;; remove-hits-generic : Lof[Posn] Lof[Posn] -> Lof[Posn]
;; Given a list of posns representing invaders and a list of posns
;; representing bullets, will return a list of posns representing either
;; invaders or bullets that have not been hit
(define (remove-hits-generic lop1 lop2)
  (cond
    [(or (empty? lop1) (empty? lop2)) lop2]
    [(hit-generic? (first lop2) lop1) (remove-hits-generic lop1 (rest lop2))]
    [else (cons (first lop2) (remove-hits-generic lop1 (rest lop2)))]))

;; Tests
(check-expect (remove-hits-generic empty empty) empty)
(check-expect (remove-hits-generic (list (make-posn 10 10) (make-posn 100 100))
                                   (list (make-posn 10 10) (make-posn 200 200)))
              (list (make-posn 200 200)))

;; hit-generic? : Posn Lof[Posn] -> Boolean
;; Given a posn representing either an invader or a bullet
;; and a list of posns of the other type, will return true
;; if there is a hit
(define (hit-generic? p lop) 
  (local 
    [(define (bullet-inside z)
       (and (<= (posn-x p) (+ (posn-x z) (/ INVADER-SIDE 2)))
            (>= (posn-x p) (- (posn-x z) (/ INVADER-SIDE 2)))
            (<= (posn-y p) (+ (posn-y z) (/ INVADER-SIDE 2)))))]
    (foldr is-true? false (map bullet-inside lop))))

;; is-true? : Boolean Boolean -> Boolean
;; Given two booleans, will return true if one is true
(define (is-true? b1 b2)
  (or b1 b2))

;; Tests
(check-expect (hit-generic? (make-posn 10 10) empty) false)
(check-expect (hit-generic? (make-posn 10 10) 
                            (list (make-posn 100 100) (make-posn 10 10))) true)


;; bullet-in-bounds? : Bullet -> Boolean
;; Given a bullet posn, will return true if it is not out
;; of bounds

(define (bullet-in-bounds? b)
  (not (or (<= (posn-y b) 0) (>= (posn-y b) HEIGHT))))

;; Tests
(check-expect (bullet-in-bounds? (make-posn 600 600)) false)
(check-expect (bullet-in-bounds? (make-posn 10 10)) true)



;; ship-hit : Ship Lof[Bullet] -> Boolean 
;; true if a bullet hit the ship, false otherwise

(define (ship-hit s lob)
  (cond
    [(empty? lob) false]
    [(and (<= (posn-x (first lob)) (+ (posn-x (ship-loc s)) (/ SHIP-WIDTH 2)))
          (>= (posn-x (first lob)) (- (posn-x (ship-loc s)) (/ SHIP-WIDTH 2)))
          (>= (posn-y (first lob)) (- (posn-y (ship-loc s)) (/ SHIP-HEIGHT 2)))
          (<= (posn-y (first lob)) (+ (posn-y (ship-loc s)) (/ SHIP-HEIGHT 2)))) true]
    [else (ship-hit s (rest lob))]))

;; Tests
(check-expect (ship-hit (make-ship 'left (make-posn 300 300)) empty) false)
(check-expect (ship-hit (make-ship 'left (make-posn 300 300)) (list (make-posn 10 10) (make-posn 300 300))) true)
(check-expect (ship-hit (make-ship 'left (make-posn 300 300)) (list (make-posn 10 10) (make-posn 400 480))) false)

;; posn=? : Posn Posn -> Boolean 
;; Given two Posns, will return true if they have the same coordinates, 
;; false otherwise
(define (posn=? p1 p2)
  (and (= (posn-x p1) (posn-x p2))
       (= (posn-y p1) (posn-y p2))))

;; Tests
(check-expect (posn=? (make-posn 300 300) (make-posn 300 300)) true)
(check-expect (posn=? (make-posn 300 300) (make-posn 400 400)) false)

;;==================================================================
;;==================================================================

;; END GAME

;; end-game? : World -> Boolean 
;; true if one of the condition that end the game has been met, false otherwise
(define (end-game? w)
  (or (ship-hit (world-ship w) 
                (world-invader-bullets w))
      (empty? (world-invaders w))))

;; Tests
(check-expect (end-game? (make-world 
                          (make-ship 'left (make-posn 10 10))
                          (list (make-posn 100 100))
                          (list (make-posn 100 400))
                          (list (make-posn 10 10)))) true)
(check-expect (end-game? (make-world 
                          (make-ship 'left (make-posn 10 10))
                          (list (make-posn 100 100))
                          (list (make-posn 100 400))
                          (list (make-posn 200 200)))) false)



;;=====================================================================
;;=====================================================================

BIG BANG

(big-bang WORLD-INIT
          (to-draw world-draw)
          (on-tick world-step 0.15)
          (on-key key-handler)
          (stop-when end-game?))