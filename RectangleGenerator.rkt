#lang racket
(require rackunit)
(require rackunit/text-ui)
(require 2htdp/universe)
(require 2htdp/image)

(provide World%
         Rectangle%
         Target%
         make-world
         run)

#|
+––––––––––––––––––––––––––––+            +–––––––––––––––––––––––––+          
|                            |            |                         |          
|   World<%>                 |            |   Shape<%>              |<+–––––––+
|                            |            |                         |         |
+––––––––––––––––––––––––––––+            +–––––––––––––––––––––––––+         |
              ^                                                               |
              +                           +–––––––––––––––––––––––––+         |
+–––––––––––––+––––––––––––––+            |                         |         |
|   World%                   |            |   Target%               |         |
|    - Target%               |            |                         +–––––––––+
|    - Listof<Shape<%>>      |            |                         |         |
|                            |            |                         |         |
+––––––––––––––––––––––––––––+            +–––––––––––––––––––––––––+         |
                                                                              |
                                                                              |
                                          +–––––––––––––––––––––––––+         |
                                          |                         |         |
                                          |   Rectangle%            |         |
                                          |                         +–––––––––+
                                          |                         |          
                                          |                         |          
                                          +–––––––––––––––––––––––––+          
|#
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;                                                                           
;                                                                           
;                                                                           
;   ;;;;;;       ;    ;;;;;;;    ;         ;;;;;;    ;;;;;;  ;;;;;   ;;;;;  
;   ;    ;;     ; ;      ;      ; ;        ;    ;;   ;       ;      ;;    ; 
;   ;      ;    ; ;      ;      ; ;        ;      ;  ;       ;      ;       
;   ;      ;   ;   ;     ;     ;   ;       ;      ;  ;       ;      ;;      
;   ;      ;   ;   ;     ;     ;   ;       ;      ;  ;;;;;;  ;;;;;   ;;;;;  
;   ;      ;  ;;;;;;;    ;    ;;;;;;;      ;      ;  ;       ;           ;; 
;   ;      ;  ;     ;    ;    ;     ;      ;      ;  ;       ;            ; 
;   ;    ;;   ;     ;    ;    ;     ;      ;    ;;   ;       ;      ;    ;; 
;   ;;;;;;   ;       ;   ;   ;       ;     ;;;;;;    ;;;;;;  ;       ;;;;;  
;                                                                           
;                                                                           
;                                                                           
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;A RelevantKeyEvent is a partition of KeyEvent into the following categories:
;; - "r"      (interp: creates a new Rectangle% traveling right whose center is
;;             at the center of target, if possible)
;; - any other KeyEvent (interp: ignore)

;;TEMPLATE:
;;rke-fn: RelevantKeyEvent -> ??
;;(define (rke-fn kev)
;;  (cond 
;;    [(key=? kev "r") ...]
;;    [else ...]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;A RelevantMouseEvent is a partition of MouseEvent into the following 
;; categories:
;; - "button-down"         (interp: maybe select a Shape%)
;; - "drag"                (interp: maybe drag a Shape%)
;; - "button-up"           (interp: unselect all(?) Shape%s)
;; - any other mouse event (interp: ignored)

;;TEMPLATE:
;;rme-fn: RelevantMouseEvent -> ??
;;(define (rme-fn mev)
;;  (cond
;;    [(mouse=? mev "button-down") ...]
;;    [(mouse=? mev "drag") ...]
;;    [(mouse=? mev "button-up") ...]
;;    [else ...]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;A Listof<Rectangle%> is one of
;; - empty
;; - (cons Rectangle% Listof<Rectangle%>


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;                                                                            
;                                                                            
;                                                                            
;     ;;;;     ;;;;    ;;     ;   ;;;;;  ;;;;;;; ;  ;;     ; ;;;;;;;  ;;;;;  
;    ;    ;   ;    ;   ;;     ;  ;;    ;    ;    ;  ;;     ;    ;    ;;    ; 
;   ;        ;      ;  ; ;    ;  ;          ;    ;  ; ;    ;    ;    ;       
;   ;        ;      ;  ;  ;   ;  ;;         ;    ;  ;  ;   ;    ;    ;;      
;   ;        ;      ;  ;  ;;  ;   ;;;;;     ;       ;  ;;  ;    ;     ;;;;;  
;   ;        ;      ;  ;   ;  ;       ;;    ;       ;   ;  ;    ;         ;; 
;   ;        ;      ;  ;    ; ;        ;    ;       ;    ; ;    ;          ; 
;    ;    ;   ;    ;   ;     ;;  ;    ;;    ;       ;     ;;    ;    ;    ;; 
;     ;;;;     ;;;;    ;     ;;   ;;;;;     ;       ;     ;;    ;     ;;;;;  
;                                                                            
;                                                                            
;                                                                            
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(define CANVAS-WIDTH 400)

(define CANVAS-HEIGHT 500)

(define EMPTY-CANVAS (empty-scene 400 500))

;;The width of all Rectangle%s
(define RECT-WIDTH 30)   

;;The height of all Rectangle%s
(define RECT-HEIGHT 20) 

;;Half the width of all Rectangle%s
(define HALF-RECT-WIDTH (/ RECT-WIDTH 2))     

;;Half the height of all Rectangle%s
(define HALF-RECT-HEIGHT (/ RECT-HEIGHT 2))

;;The radius of the Target%
(define TARGET-RADIUS 5)


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;                                                                           
;                                                                           
;                                                                           
;   ;  ;;     ; ;;;;;;; ;  ;;;;;   ;;;;;     ;       ;;;;   ;;;;;;   ;;;;;  
;   ;  ;;     ;    ;    ;  ;    ;  ;        ; ;     ;    ;  ;       ;;    ; 
;   ;  ; ;    ;    ;    ;  ;    ;  ;        ; ;    ;        ;       ;       
;   ;  ;  ;   ;    ;    ;  ;    ;  ;       ;   ;   ;        ;       ;;      
;   ;  ;  ;;  ;    ;       ;;;;;   ;;;;;   ;   ;   ;        ;;;;;;   ;;;;;  
;   ;  ;   ;  ;    ;       ;   ;   ;      ;;;;;;;  ;        ;            ;; 
;   ;  ;    ; ;    ;       ;    ;  ;      ;     ;  ;        ;             ; 
;   ;  ;     ;;    ;       ;    ;  ;      ;     ;   ;    ;  ;       ;    ;; 
;   ;  ;     ;;    ;       ;     ; ;     ;       ;   ;;;;   ;;;;;;   ;;;;;  
;                                                                           
;                                                                           
;                                                                           
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(define World<%>
  (interface ()
    
    ;; -> Void
    ;; EFFECT: updates this World to its state following a tick
    on-tick                             
    
    ;; Integer Integer MouseEvent -> Void
    ;; EFFECT: updates this World to its state following the given
    ;; MouseEvent;
    on-mouse
    
    ;; KeyEvent -> Void
    ;; EFFECT: updates this World to its state following the given
    ;; Key event.
    on-key
    
    ;; Scene -> Scene
    ;; RETURNS: a Scene like the given one, but with this object drawn
    ;; on it.
    add-to-scene  
    
    ;; -> Number
    ;; RETURN the x and y coordinates of the target
    get-x
    get-y
    
    ;; -> Boolean
    ;; RETURNS: Is the target selected?
    get-selected?
    
    ;; -> ListOf<Shape<%>>
    ;; RETURNS: the list of shapes in the world
    get-shapes
    
    ))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(define Shape<%>
  (interface ()
    
    ;; -> Void
    ;; EFFECT: updates this Shape to its state following a tick
    on-tick                             
    
    ;; Integer Integer MouseEvent -> Void
    ;; EFFECT: updates this Shape to its state following the given
    ;; MouseEvent;
    on-mouse
    
    ;; KeyEvent -> Void
    ;; EFFECT: updates this Shape to its state following the given
    ;; Key event.
    on-key
    
    ;; Scene -> Scene
    ;; RETURNS: a Scene like the given one, but with this object drawn
    ;; on it.
    add-to-scene
    
    ;; -> Number
    ;; RETURN: the x and y coordinates of the center of this shape.
    get-x
    get-y
    
    ;; -> Boolean
    ;; Is this shape currently selected?
    is-selected?
    
    ;; -> String
    ;; RETURNS: either "red" or "green", depending on the color in
    ;; which this shape would be displayed if it were displayed now.
    get-color
    
    
    ;; Number -> Number
    ;; Returns an appropriate x or y value for a Shape%, based on its size and 
    ;;  the size of the Canvas (prevents Shape%s' edges from crossing the edge
    ;;  of the Canvas)
    limit-x
    limit-y
    
    ;; Number Number -> Boolean
    ;; GIVEN: a location on the canvas
    ;; RETURNS: true iff the location is inside this Shape<%>.
    in-shape?
    
    ))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;                                                              
;                                                              
;                                                              
;     ;;;;   ;         ;      ;;;;;    ;;;;;   ;;;;;;   ;;;;;  
;    ;    ;  ;        ; ;    ;;    ;  ;;    ;  ;       ;;    ; 
;   ;        ;        ; ;    ;        ;        ;       ;       
;   ;        ;       ;   ;   ;;       ;;       ;       ;;      
;   ;        ;       ;   ;    ;;;;;    ;;;;;   ;;;;;;   ;;;;;  
;   ;        ;      ;;;;;;;       ;;       ;;  ;            ;; 
;   ;        ;      ;     ;        ;        ;  ;             ; 
;    ;    ;  ;      ;     ;  ;    ;;  ;    ;;  ;       ;    ;; 
;     ;;;;   ;;;;;;;       ;  ;;;;;    ;;;;;   ;;;;;;   ;;;;;  
;                                                              
;                                                              
;                                                              
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;                                                
;                                                
;                                                
;  ;    ;    ;   ;;;;    ;;;;;   ;      ;;;;;;   
;  ;    ;    ;  ;    ;   ;    ;  ;      ;    ;;  
;   ;   ;   ;  ;      ;  ;    ;  ;      ;      ; 
;   ;  ; ;  ;  ;      ;  ;    ;  ;      ;      ; 
;   ;  ; ;  ;  ;      ;  ;;;;;   ;      ;      ; 
;    ; ; ; ;   ;      ;  ;   ;   ;      ;      ; 
;    ; ; ; ;   ;      ;  ;    ;  ;      ;      ; 
;     ;   ;     ;    ;   ;    ;  ;      ;    ;;  
;     ;   ;      ;;;;    ;     ; ;;;;;; ;;;;;;   
;                                                
;                                                
;                                                
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; A World is a 
;;  (new World% [target Target%] [shapes Listof<Shape<%>>] [speed PosInt])
;;  that represents a canvas with a Target% and other Shape<%>s on it.
;; World% -- a class that satisfies the World<%> interface (shown above).
(define World% 
  (class* object% (World<%>)
    (super-new)
    (init-field
     
     ;;A Target% -- The circle displayed on the canvas
     target
     
     ;;A ListOf<Rectangle%> -- The list of Shape<%>s currently displayed on 
     ;; the canvas
     shapes
     
     ;;A PosInt -- The "speed" of the Rectangle%s in pixels per tick
     speed)
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;;METHODS
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; on-tick: -> Void
    ;; EFFECT: Updates this World to its state following a tick
    (define/public (on-tick)
      ;;(set! target (send target on-tick))
      (for-each (λ (shape) (send shape on-tick))
                shapes))
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; on-mouse: Integer Integer MouseEvent -> Void
    ;; GIVEN: the x and y coordinates of a mouse event, and the mouse event
    ;; EFFECT: updates this World to its state following the given MouseEvent
    (define/public (on-mouse x y evt)
      (send target on-mouse x y evt)
      (for-each (λ (shape) (send shape on-mouse x y evt))
                shapes))
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; on-key: KeyEvent -> Void
    ;; EFFECT: updates this World to its state following the given KeyEvent.
    (define/public (on-key kev)
      (let ([maybe-shape (send target generate-maybe-shape kev speed)])
        (if (not (false? maybe-shape))
            (set! shapes (cons maybe-shape shapes))
            this)))
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; add-to-scene: Scene -> Scene
    ;; RETURNS: a Scene like the given one, but with this object drawn
    ;; on it.
    (define/public (add-to-scene scene)
      ;; then tell each shape to add itself to the scene
      (foldr
       (λ (shape scene-so-far)
         (send shape add-to-scene scene-so-far))
       (send target add-to-scene scene)
       shapes))
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; Getters:
    ;; get-x : -> Posint
    ;; get-y : -> Posint
    ;; get-selected: -> Boolean
    ;; get-target : -> Target
    ;; get-rectangles : -> List<Rectangle%>
    ;; get-shapes : -> ListOf<Shape<%>>
    (define/public (get-x) (send target get-x))
    (define/public (get-y) (send target get-y))
    (define/public (get-selected?) (send target get-selected?))
    (define/public (get-target) target)
    (define/public (get-rectangles) (shapes))
    ;; Took out target, since get-shapes should only return rectangles
    (define/public (get-shapes) shapes)
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;;world-equal? : Target% Listof<Shape<%>> -> Boolean
    ;;RETURNS: True if this World<%> has the same Target% and Shape<%>s
    (define/public (world-equal? a-target some-shapes)
      (and 
       (send target targ-equal? a-target)
       (andmap (λ (a-shape) (send this has-shape? a-shape))
               some-shapes)))
    
    ;;has-shape? : Shape<%> -> Boolean
    ;;RETURNS: True if the given Shape% is in this World%'s list of Shape<%>s 
    (define/public (has-shape? a-shape)
      (ormap (λ (shape) (send shape rect-equal? a-shape))
             shapes))
    
    ))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;                                                                              
;                                                                              
;                                                                              
;   ;;;;;   ;;;;;;    ;;;;  ;;;;;;;    ;     ;;     ;    ;;;;;   ;      ;;;;;; 
;   ;    ;  ;        ;    ;    ;      ; ;    ;;     ;   ;     ;  ;      ;      
;   ;    ;  ;       ;          ;      ; ;    ; ;    ;  ;         ;      ;      
;   ;    ;  ;       ;          ;     ;   ;   ;  ;   ;  ;         ;      ;      
;   ;;;;;   ;;;;;;  ;          ;     ;   ;   ;  ;;  ;  ;    ;;;  ;      ;;;;;; 
;   ;   ;   ;       ;          ;    ;;;;;;;  ;   ;  ;  ;      ;  ;      ;      
;   ;    ;  ;       ;          ;    ;     ;  ;    ; ;  ;      ;  ;      ;      
;   ;    ;  ;        ;    ;    ;    ;     ;  ;     ;;   ;     ;  ;      ;      
;   ;     ; ;;;;;;    ;;;;     ;   ;       ; ;     ;;    ;;;;;   ;;;;;; ;;;;;; 
;                                                                              
;                                                                              
;                                                                              
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; A Rectangle is a (new Rectangle% [x Number][y Number][selected? Boolean]
;;                   [x-off Number][y-off Number][speed Int])
;; A Rectangle represents a rectangle.
;;Constructor: (new Rectangle% [x ][y ][selected? ] [x-off ][y-off ] [speed ])
(define Rectangle%
  (class* object% (Shape<%>) 
    (super-new)
    (init-field
     
     ;;The x position of the center of this Rectangle%, in pixels relative to 
     ;; the upper-left corner of the canvas
     x            
     
     ;;The y position of the center of this Rectangle%
     y
     
     ;;Whether or not this Rectangle% is selected
     [selected? false]
     
     ;;A Number which, when added to the x position of a mouse click, yields
     ;; the x position of this Rectangle%'s center 
     [x-off 0]
     
     ;;A Number which, when added to the y position of a mouse click, yields
     ;; the y position of this Rectangle%'s center
     [y-off 0]
     
     
     ;;The Rectangle%'s speed, in pixels/tick
     [speed 1])
    
    
    ;;The Image that corresponds to this Rectangle%
    (field [IMG (rectangle RECT-WIDTH RECT-HEIGHT "outline" "green")])
    
    ;;A Number that represents the highest possible x value of the center of 
    ;; this Rectangle%
    (field [RIGHT-BOUNDARY (- CANVAS-WIDTH HALF-RECT-WIDTH 1)])
    
    ;;A Number that represents the highest possible y value of the center of 
    ;; this Rectangle%
    (field [BOTTOM-BOUNDARY (- CANVAS-HEIGHT HALF-RECT-HEIGHT 1)])
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;;METHODS
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; on-tick: -> Void
    ;; EFFECT: updates this Rectangle% to its state following a tick
    (define/public (on-tick)
      (if selected?
          this
          (begin 
            (set! x (+ x speed)) 
            (send this bounce))))
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; bounce: -> Void
    ;; EFFECT: If this Rectangle% has crossed a boundary, update its position so
    ;;  it is adjacent to the boundary and reverse its speed 
    (define/public (bounce)
      (cond
        [(< x HALF-RECT-WIDTH) 
         (set!-values (x speed) (values HALF-RECT-WIDTH (* -1 speed)))]
        [(> x RIGHT-BOUNDARY) 
         (set!-values (x speed) (values RIGHT-BOUNDARY (* -1 speed)))]
        [else this]))
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; on-key: KeyEvent -> Void
    ;; EFFECT: updates this Shape to its state following the given KeyEvent.
    ;; DETAILS: a rectangle ignores key events
    (define/public (on-key kev)
      this)      
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; on-mouse : Integer Integer MouseEvent -> Void
    ;; GIVEN: the location of a mouse event, and the mouse event
    ;; EFFECT: updates this Rectangle% to its state following the given MouseEvent
    (define/public (on-mouse mouse-x mouse-y evt)
      (cond
        [(mouse=? evt "button-down")
         (send this rectangle-after-button-down mouse-x mouse-y)]
        [(mouse=? evt "drag") 
         (send this rectangle-after-drag mouse-x mouse-y)]
        [(mouse=? evt "button-up")
         (send this rectangle-after-button-up)]
        [else this]))
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; rectangle-after-button-down : Number Number -> Void
    ;; GIVEN: the location of a mouse event
    ;; EFFECT: Updates this rectangle to the correct state following a button
    ;; down at the given location
    ;; DETAILS:  If the event is inside the rectangle, returns a rectangle like
    ;; this one, except that it is selected and its x-off and y-off are updated
    ;; appropriately. Otherwise does nothing.
    (define/public (rectangle-after-button-down mouse-x mouse-y)
      (if (send this in-shape? mouse-x mouse-y)
          (set!-values (selected? x-off y-off) 
                       (values true (- x mouse-x)(- y mouse-y)))
          this))
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; rectangle-after-drag : Number Number -> Void
    ;; GIVEN: the location of a mouse event
    ;; EFFECT: Updates this Rectangle% to the correct state following a drag at
    ;; the given location
    ;; DETAILS: if rectangle is selected, move the rectangle to the mouse 
    ;; location (offset by x- and y-off), otherwise ignore.
    (define/public (rectangle-after-drag mouse-x mouse-y)
      (if selected?
          (set!-values (x y) (values (send this limit-x (+ mouse-x x-off))
                                     (send this limit-y (+ mouse-y y-off))))
          this))
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; rectangle-after-button-up : -> Void
    ;; EFFECT: Updates this Rectangle% to the correct state following a button-up
    ;; DETAILS: button-up unselects all rectangle
    (define/public (rectangle-after-button-up)
      (set!-values (selected? x-off y-off) (values false 0 0)))
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; add-to-scene : Scene -> Scene
    ;; GIVEN: a scene
    ;; RETURNS: a scene like the given one, but with this Rectangle% drawn into
    ;; it.
    (define/public (add-to-scene scene)
      (place-image IMG x y scene))
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; in-shape? : Number Number -> Boolean
    ;; GIVEN: a location on the canvas
    ;; RETURNS: true iff the location is inside this Rectangle%.
    (define/public (in-shape? mouse-x mouse-y)
      (and (>= mouse-x (- x HALF-RECT-WIDTH))
           (<= mouse-x (+ x HALF-RECT-WIDTH))
           (>= mouse-y (- y HALF-RECT-HEIGHT))
           (<= mouse-y (+ y HALF-RECT-HEIGHT))))
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; is-selected? : -> Boolean
    ;; RETURNS: true if this Rectangle% is selected, else false
    (define/public (is-selected?) 
      (send this get-selected?))
    
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; limit-x: Number -> Number
    ;; limit-y: Number -> Number
    ;; GIVEN: a number for the new x or y position of this Rectangle% after a 
    ;; drag
    ;; RETURNS: The maximum or minimum x value for the center of the Rectangle%
    ;; if it is out of bounds, else returns the given number
    (define/public (limit-x new-x)
      (cond
        [(< new-x HALF-RECT-WIDTH) HALF-RECT-WIDTH]
        [(> new-x RIGHT-BOUNDARY) RIGHT-BOUNDARY]
        [else new-x]))
    
    (define/public (limit-y new-y)
      (cond
        [(< new-y HALF-RECT-HEIGHT) HALF-RECT-HEIGHT]
        [(> new-y BOTTOM-BOUNDARY) BOTTOM-BOUNDARY]
        [else new-y]))
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; get-color: -> String
    ;; RETURNS: either "red" or "green", depending on the color in
    ;; which this shape would be displayed if it were displayed now.
    (define/public (get-color)
      "green")
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; getters:
    ;; get-x : -> PosInt
    ;; get-y : -> PosInt
    ;; get-x-off : -> Int
    ;; get-y-off : -> Int
    ;; get-speed : -> Int
    ;; get-selected? : -> Boolean
    (define/public (get-x) x)
    (define/public (get-y) y)
    (define/public (get-x-off) x-off)
    (define/public (get-y-off) y-off)
    (define/public (get-speed) speed)
    (define/public (get-selected?) selected?)
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;;rect-equal?: Rectangle% -> Boolean
    ;;GIVEN: a Rectangle%
    ;;RETURN: True if all values match the values of this Rectangle%
    (define/public (rect-equal? that)
      (and
       (= x (send that get-x))
       (= y (send that get-y))
       (equal? selected? (send that get-selected?))
       (= x-off (send that get-x-off))
       (= y-off (send that get-y-off))
       (= speed (send that get-speed))))
    
    
    ))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;                                                   
;                                                   
;                                                   
;  ;;;;;;;    ;     ;;;;;     ;;;;;   ;;;;;; ;;;;;;;
;     ;      ; ;    ;    ;   ;     ;  ;         ;   
;     ;      ; ;    ;    ;  ;         ;         ;   
;     ;     ;   ;   ;    ;  ;         ;         ;   
;     ;     ;   ;   ;;;;;   ;    ;;;  ;;;;;;    ;   
;     ;    ;;;;;;;  ;   ;   ;      ;  ;         ;   
;     ;    ;     ;  ;    ;  ;      ;  ;         ;   
;     ;    ;     ;  ;    ;   ;     ;  ;         ;   
;     ;   ;       ; ;     ;   ;;;;;   ;;;;;;    ;   
;                                                   
;                                                   
;                                                   
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; A Target is a (new Target% [x Number][y Number][selected? Boolean]
;;                [x-off Number][y-off Number])
;; A Target represents a circle on the canvas.
;;Constructor: (new Target% [x ][y ][selected? ][x-off ][y-off ])
(define Target%
  (class* object% (Shape<%>)
    (super-new)
    (init-field
     
     ;;The x position of the center of this Target%, in pixels relative to the
     ;; upper-left corner of the canvas
     x            
     
     ;;The y position of the center of this Target%
     y
     
     ;;Whether or not this Target% is selected
     [selected? false]
     
     ;;A Number which, when added to the x position of a mouse click, yields
     ;; the x position of this Target%'s center 
     [x-off 0]
     
     ;;A Number which, when added to the y position of a mouse click, yields
     ;; the y-position of this Target%'s center 
     [y-off 0])
    
    
    ;;Private data for objects of this class.
    ;;These can depend on the init-fields.
    
    ;;The Image corresponding to this Target%
    (field [IMG (circle TARGET-RADIUS "outline" "red")])     
    
    ;;A Number that represents the highest possible x value of the center of 
    ;; this Target%
    (field [RIGHT-BOUNDARY (- CANVAS-WIDTH TARGET-RADIUS 1)])
    
    ;;A Number that represents the highest possible y value of the center of 
    ;; this Target%
    (field [BOTTOM-BOUNDARY (- CANVAS-HEIGHT TARGET-RADIUS 1)])
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;;METHODS
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; on-tick : -> Void
    ;; EFFECT: Does nothing, as Target% ignores on-tick
    (define/public (on-tick)
      this)
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; on-key : KeyEvent -> Target
    ;; EFFECT: Does nothing, as Target% ignores on-key
    ;; DETAILS: a target ignores key events
    (define/public (on-key kev)
      this)      
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; generate-maybe-shape: KeyEvent Number -> MaybeShape (A Shape<%> or False)
    ;; GIVEN: A KeyEvent and a Number for the speed of any generated Shape<%>
    ;; RETURNS: A Shape<%> that corresponds to the given KeyEvent, with the 
    ;;  given speed IFF it will fit
    (define/public (generate-maybe-shape kev speed)
      (cond
        [(and (key=? kev "r") (send this rect-will-fit?))
         (new Rectangle% [x x][y y][speed speed])]
        #;[(and (key=? kev "t") (send this tri-will-fit?))
           (new Triangle% [x x][y y][speed speed])]
        [else false]))
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; rect-will-fit?: -> Boolean
    ;; RETURNS: true if a new Rectangle% will fit entirely on the canvas, else 
    ;; false
    (define/public (rect-will-fit?)
      (let ([top (- y HALF-RECT-HEIGHT)]
            [bottom (+ y HALF-RECT-HEIGHT)]
            [left (- x HALF-RECT-WIDTH)]
            [right (+ x HALF-RECT-WIDTH)])
        (and (> top 0)(< bottom CANVAS-HEIGHT)
             (> left 0)(< right CANVAS-WIDTH))))
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; on-mouse : Number Number MouseEvent -> Void
    ;; GIVEN: the location of a mouse event, and the mouse event
    ;; EFFECT: updates this Target% to its state following the given MouseEvent
    (define/public (on-mouse mouse-x mouse-y evt)
      (cond
        [(mouse=? evt "button-down")
         (send this target-after-button-down mouse-x mouse-y)]
        [(mouse=? evt "drag") 
         (send this target-after-drag mouse-x mouse-y)]
        [(mouse=? evt "button-up")
         (send this target-after-button-up)]
        [else this]))
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;;target-after-button-down : Number Number -> Void
    ;;GIVEN: the location of a mouse event
    ;;EFFECT: Updates the target to its correct state following a button down at
    ;; the given location
    ;;DETAILS:  If the event is inside the Target%, returns a Target% like this
    ;; one, except that it is selected and has updated x-off and y-off. 
    ;; Otherwise does nothing.
    (define/public (target-after-button-down mouse-x mouse-y)
      (if (send this in-shape? mouse-x mouse-y)
          (set!-values (selected? x-off y-off) 
                       (values true (- x mouse-x) (- y mouse-y)))
          this))
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; in-shape? : Number Number -> Boolean
    ;; GIVEN: a location on the canvas
    ;; RETURNS: true iff the location is inside this Target%
    (define/public (in-shape? mouse-x mouse-y)
      (let ([x-diff (- x mouse-x)]
            [y-diff (- y mouse-y)])
        (<= (+ (* x-diff x-diff) (* y-diff y-diff))
            (* TARGET-RADIUS TARGET-RADIUS))))
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;;target-after-drag : Number Number -> Void
    ;;GIVEN: the location of a mouse event
    ;;EFFECT: Updates the Target% to its correct state following a drag to the
    ;; given location
    ;;DETAILS: if Target% is selected, move the target to the mouse location,
    ;;otherwise ignore.
    (define/public (target-after-drag mouse-x mouse-y)
      (if selected?
          (set!-values (x y) (values (send this limit-x (+ mouse-x x-off))
                                     (send this limit-y (+ mouse-y y-off))))
          this))
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; limit-y: Number -> Number
    ;; limit-x: Number -> Number
    ;; GIVEN: a number for the new x or y position of this Target% after a drag
    ;; RETURNS: the radius distance from the border of the canvas if the given 
    ;; number is out of bounds, else returns the given number
    (define/public (limit-x new-x)
      (cond
        [(< new-x TARGET-RADIUS) TARGET-RADIUS]
        [(> new-x RIGHT-BOUNDARY) RIGHT-BOUNDARY]
        [else new-x]))
    
    (define/public (limit-y new-y)
      (cond
        [(< new-y TARGET-RADIUS) TARGET-RADIUS]
        [(> new-y BOTTOM-BOUNDARY) BOTTOM-BOUNDARY]
        [else new-y]))
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; target-after-button-up : -> Void
    ;;EFFECT: Updates the target to its correct state following a button-up
    ;; DETAILS: button-up unselects the target
    (define/public (target-after-button-up)
      (set! selected? false))
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; add-to-scene : Scene -> Scene
    ;; RETURNS: a scene like the given one, but with the target painted on it.
    (define/public (add-to-scene scene)
      (place-image IMG x y scene))
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;;is-selected? : -> Boolean
    ;;RETURNS: true if target is selected, else false
    (define/public (is-selected?) 
      (send this get-selected?))
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; get-color: -> String
    ;; RETURNS: "red", the color of the/any Target%
    (define/public (get-color)
      "red")
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;;getters:
    ;; get-x : -> PosInt
    ;; get-y : -> PosInt
    ;; get-x-off : -> Int
    ;; get-y-off : -> Int
    ;; get-radius: -> PosInt
    ;; get-selected? : -> Boolean
    (define/public (get-x) x)
    (define/public (get-y) y)
    (define/public (get-x-off) x-off)
    (define/public (get-y-off) y-off)
    (define/public (get-radius) TARGET-RADIUS)
    (define/public (get-selected?) selected?)
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;;targ-equal?: Target% -> Boolean
    ;;GIVEN: a Target%
    ;;RETURN: True if all values match the values of this Target%
    (define/public (targ-equal? that)
      (and
       (= x (send that get-x))
       (= y (send that get-y))
       (equal? selected? (send that get-selected?))
       (= x-off (send that get-x-off))
       (= y-off (send that get-y-off))))
    
    ))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;                                                                             
;                                                                             
;                                                                             
;   ;;;;;  ;      ;  ;;     ;    ;;;;  ;;;;;;; ;    ;;;;    ;;     ;   ;;;;;  
;   ;      ;      ;  ;;     ;   ;    ;    ;    ;   ;    ;   ;;     ;  ;;    ; 
;   ;      ;      ;  ; ;    ;  ;          ;    ;  ;      ;  ; ;    ;  ;       
;   ;      ;      ;  ;  ;   ;  ;          ;    ;  ;      ;  ;  ;   ;  ;;      
;   ;;;;;  ;      ;  ;  ;;  ;  ;          ;    ;  ;      ;  ;  ;;  ;   ;;;;;  
;   ;      ;      ;  ;   ;  ;  ;          ;    ;  ;      ;  ;   ;  ;       ;; 
;   ;      ;      ;  ;    ; ;  ;          ;    ;  ;      ;  ;    ; ;        ; 
;   ;      ;;    ;;  ;     ;;   ;    ;    ;    ;   ;    ;   ;     ;;  ;    ;; 
;   ;        ;;;;    ;     ;;    ;;;;     ;    ;    ;;;;    ;     ;;   ;;;;;  
;                                                                             
;                                                                             
;                                                                             
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; make-world : Integer -> World%
;; GIVEN: a Speed in pixels per tick
;; RETURNS: Creates a world with no shapes, but in which any shapes
;; created in the future will travel at the given speed.
;; STRATEGY: Function composition
(define (make-world speed)
  (new World% 
       [target (new Target% 
                    [x (/ CANVAS-WIDTH 2)]
                    [y (/ CANVAS-HEIGHT 2)])]
       [shapes empty]
       [speed speed]))                   


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; run : PosNum PosInt -> World%
;; Given a frame rate (in seconds/tick) and a shape-speed (in pixels/tick),
;; creates and runs a world.  Returns the final state of the world.
(define (run frame-rate shape-speed)
  (big-bang (make-world shape-speed)
            (on-tick
             (lambda (w) (send w on-tick) w)
             frame-rate)
            (on-key
             (lambda (w kev) (send w on-key kev) w))
            (on-mouse
             (lambda (w mx my me) (send w on-mouse mx my me) w))
            (to-draw
             (lambda (w) (send w add-to-scene EMPTY-CANVAS)))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(run .25 5)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;                                          
;                                          
;                                          
;  ;;;;;;; ;;;;;;   ;;;;;  ;;;;;;;  ;;;;;  
;     ;    ;       ;;    ;    ;    ;;    ; 
;     ;    ;       ;          ;    ;       
;     ;    ;       ;;         ;    ;;      
;     ;    ;;;;;;   ;;;;;     ;     ;;;;;  
;     ;    ;            ;;    ;         ;; 
;     ;    ;             ;    ;          ; 
;     ;    ;       ;    ;;    ;    ;    ;; 
;     ;    ;;;;;;   ;;;;;     ;     ;;;;;  
;                                          
;                                          
;                                          
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(run-tests 
 (test-suite
  "All Tests"
  (test-case 
   "Basic run through"
   (define world1 (make-world 5))
   (check-equal? (send world1 world-equal? 
                       (new Target% 
                            [x 200]
                            [y 250])
                       empty)
                 true)
   (send world1 on-key "r")
   (check-equal? (send world1 world-equal? 
                       (new Target% 
                            [x 200]
                            [y 250])
                       (list 
                        (new Rectangle% [x 200][y 250][speed 5])))
                 true)
   (send world1 on-key "q")
   (check-equal? (send world1 world-equal? 
                       (new Target% 
                            [x 200]
                            [y 250])
                       empty)
                 true)
   (send world1 on-mouse 50 50 "move")
   (check-equal? (send world1 world-equal? 
                       (new Target% 
                            [x 200]
                            [y 250])
                       empty)
                 true)
   (send world1 on-tick)
   (check-equal? (send world1 world-equal? 
                       (new Target% 
                            [x 200]
                            [y 250])
                       (list 
                        (new Rectangle% [x 205][y 250][speed 5]))) 
                 true)
   (send world1 on-mouse 210 246 "button-down")
   (check-equal? (send world1 world-equal? 
                       (new Target% 
                            [x 200]
                            [y 250])
                       (list 
                        (new Rectangle% [x 205][y 250][selected? true]
                             [x-off -5][y-off 4][speed 5])))
                 true)
   (send world1 on-tick)
   (check-equal? (send world1 world-equal? 
                       (new Target% 
                            [x 200]
                            [y 250])
                       (list 
                        (new Rectangle% [x 205][y 250][selected? true]
                             [x-off -5][y-off 4][speed 5])))
                 true)
   (send world1 on-mouse 2 498 "drag")
   (check-equal? (send world1 world-equal? 
                       (new Target% 
                            [x 200]
                            [y 250])
                       (list 
                        (new Rectangle% [x 15][y 489][selected? true]
                             [x-off -5][y-off 4][speed 5]))) 
                 true)
   (send world1 on-mouse 2 498 "button-up")
   (check-equal? (send world1 world-equal? 
                       (new Target% 
                            [x 200]
                            [y 250])
                       (list 
                        (new Rectangle% [x 15][y 489][selected? false]
                             [x-off 0][y-off 0][speed 5]))) 
                 true)
   (send world1 on-mouse 2 498 "button-down")
   (check-equal? (send world1 world-equal? 
                       (new Target% 
                            [x 200]
                            [y 250])
                       (list 
                        (new Rectangle% [x 15][y 489][selected? true]
                             [x-off 13][y-off -9][speed 5]))) 
                 true)
   (send world1 on-mouse 398 2 "drag")
   (check-equal? (send world1 world-equal? 
                       (new Target% 
                            [x 200]
                            [y 250])
                       (list 
                        (new Rectangle% [x 384][y 10][selected? true]
                             [x-off 13][y-off -9][speed 5]))) 
                 true)
   (send world1 on-mouse 398 2 "button-up")
   (send world1 on-tick)
   (check-equal? (send world1 world-equal? 
                       (new Target% 
                            [x 200]
                            [y 250])
                       (list 
                        (new Rectangle% [x 384][y 10][selected? false]
                             [x-off 0][y-off 0][speed -5]))) 
                 true)
   )))
