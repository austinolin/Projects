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


;; A Corner is a (list PosInt Posint)
;;
;; Interpretation: 
;; A Corner represents a corner on a rectangle, where
;; the first posInt is the x coordinate and the second is
;; the y coordinate

;;A Listof<Corner> is one of
;; - empty
;; - (cons Corner Listof<Corner>

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

;; Unselected rectangle
(define RECT-UNSELECTED (rectangle RECT-WIDTH RECT-HEIGHT "outline" "green"))

;; Selected rectangle
(define RECT-SELECTED (rectangle RECT-WIDTH RECT-HEIGHT "outline" "red"))

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
;;  (new World% [target Target%] [shapes Listof<Shape<%>>])
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
     shapes)
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;;METHODS
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; on-tick: -> Void
    ;; EFFECT: Updates this World to its state following a tick
    ;; STRATEGY: Domain Knowledge
    (define/public (on-tick)
      this)
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; on-mouse: Integer Integer MouseEvent -> Void
    ;; GIVEN: the x and y coordinates of a mouse event, and the mouse event
    ;; EFFECT: updates this World to its state following the given MouseEvent
    ;; STRATEGY: Function Composition/HOFC
    ;; EXAMPLES: Given a "button down" on an unselected rectangle, 
    ;; will return a World with a selected green rectangle
    (define/public (on-mouse x y evt)
      (send target on-mouse x y evt)
      (for-each (λ (shape) (send shape on-mouse x y evt))
                shapes))
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; on-key: KeyEvent -> Void
    ;; EFFECT: updates this World to its state following the given KeyEvent.
    ;; STRATEGY: Function composition/HOFC
    ;; EXAMPLES:
    ;; Given "r", returns a world with a rectangle created at the center of
    ;; the target, or returns the same world if the target is too close to a
    ;; border of the canvas
    (define/public (on-key kev)
      (let ([maybe-shape (send target generate-maybe-shape kev shapes)])
        (if (not (false? maybe-shape))
            (begin
              (for-each (λ (shape) (send shape add-candidates maybe-shape))
                        shapes)
              (set! shapes (cons maybe-shape shapes)))
            this)))
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; add-to-scene: Scene -> Scene
    ;; GIVEN: a scene
    ;; RETURNS: a Scene like the given one, but with this object drawn
    ;; on it.
    ;; STRATEGY: HOFC
    ;; EXAMPLES:
    ;; a scene with nothing on it, returns a scene with the target placed
    ;; at its given x and y coordinates, and the rectangles in the rectangle
    ;; list placed at their respective x and y coordinates
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

;; A Rectangle is a (new Rectangle% [x Number][y Number]
;;                     [buddy-candidates List<Shape%>][selected? Boolean]
;;                        [x-off Number][y-off Number])
;; A Rectangle represents a rectangle.
;;Constructor: (new Rectangle% [x ][y ][buddy-candidates ][selected? ]
;;                                      [x-off ][y-off ])
(define Rectangle%
  (class* object% (Shape<%>) 
    (super-new)
    (init-field
     
     ;;The x position of the center of this Rectangle%, in pixels relative to 
     ;; the upper-left corner of the canvas
     x            
     
     ;;The y position of the center of this Rectangle%
     y
     
     ;;A list of Shape% that shows the list of possible buddies left on the 
     ;;canvas
     buddy-candidates
     
     ;;Whether or not this Rectangle% is selected
     [selected? false]
     
     ;;A Number which, when added to the x position of a mouse click, yields
     ;; the x position of this Rectangle%'s center 
     [x-off 0]
     
     ;;A Number which, when added to the y position of a mouse click, yields
     ;; the y position of this Rectangle%'s center
     [y-off 0])
    
    
    
    
    ;; A list of Shape% that consists of all current buddies of the rectangle,
    ;; starts out empty
    (field [BUDDIES empty])
    
    
    ;;The String that corresponds to the color of the rectangle ("green" 
    ;; initially, else "red")
    (field [COLOR "green"])
    
    ;;The Image that corresponds to this Rectangle%
    (field [IMG RECT-UNSELECTED])
    
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
    ;; STRATEGY: Domain Knowledge
    (define/public (on-tick)
      this)
    
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; on-key: KeyEvent -> Void
    ;; EFFECT: updates this Shape to its state following the given KeyEvent.
    ;; DETAILS: a rectangle ignores key events
    ;; STRATEGY: Domain Knowledge
    (define/public (on-key kev)
      this)      
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; add-candidates : Shape% -> Void
    ;; GIVEN: a newly generated Shape%
    ;; EFFECT: Updates the list of potential buddies 
    ;; DETAILS: When a new rectangle is added, this function will add it to the
    ;; list of buddy-candidates
    ;; STRATEGY: Function Composition
    (define/public (add-candidates shape)
      (set! buddy-candidates (cons shape buddy-candidates)))
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    
    ;; on-mouse : Integer Integer MouseEvent -> Void
    ;; GIVEN: the location of a mouse event, and the mouse event
    ;; EFFECT: updates this Shape to its state following the given MouseEvent
    ;; STRATEGY: STRUCT DECOMP on evt : MouseEvent
    ;; EXAMPLES: 
    ;; "button-down" with coordinates inside of the rectangle
    ;;  -- returns same rectangle, but makes it selected
    ;;     and with the mouse distances for x and y recorded
    ;; "button-up" will unselect the rectangle
    ;; "drag" with mouse at center of rectangle to 5 pixels below
    ;; original location 
    ;;  -- returns same rectangle, but with +5 added to its
    ;;     y field.
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
    ;; DETAILS:  If the event is inside
    ;; the rectangle, returns a rectangle just like this one, except that it is
    ;; selected.  Otherwise returns the rectangle unchanged. Also turns all
    ;; buddies of the rectangle red.
    ;; STRATEGY: structural decomposition on this
    (define/public (rectangle-after-button-down mouse-x mouse-y)
      (if (send this in-shape? mouse-x mouse-y)
          (begin
            ;; Update this
            (set!-values 
             (selected? x-off y-off IMG COLOR) 
             (values true (- x mouse-x)(- y mouse-y) RECT-SELECTED "red"))
            ;; Change buddies' color to red
            (for-each 
             (λ (buddy) (send buddy buddy-rectangle-change RECT-SELECTED "red"))
             BUDDIES))
          this))
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; buddy-rectangle-change : Image String -> Void
    ;; GIVEN: an image (either RECT-SELECTED or RECT-UNSELECTED) and a 
    ;; String ("green" or "red")
    ;; EFFECT: Sets the IMG of the shape to the given image and the color
    ;; to the given String
    ;; DETAILS: if a buddy is selected, this function changes the shape to a 
    ;; red rectangle. If buddy is unselected, then turns to green rectangle
    ;; STRATEGY: Function Composition
    (define/public (buddy-rectangle-change img color)
      (set!-values (IMG COLOR) (values img color)))
    
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; rectangle-after-drag : Number Number -> Void
    ;; GIVEN: the location of a mouse event
    ;; EFFECT: Updates this rectangle to the correct state following a drag at
    ;; the given location, turns any overlapping rectangles into buddies
    ;; DETAILS: if rectangle is selected, move the rectangle to the mouse 
    ;; location, otherwise ignore.
    ;; STRATEGY: domain knowledge  (NOTE: new doesn't count)
    (define/public (rectangle-after-drag mouse-x mouse-y)
      (if selected?
          (begin
            ;; Update this
            (set!-values (x y) (values (send this limit-x (+ mouse-x x-off))
                                       (send this limit-y (+ mouse-y y-off))))
            ;; Add overlapping shapes to buddies
            (send this add-buddies buddy-candidates)
            ;; Turn buddies red
            (for-each 
             (λ (buddy) (send buddy buddy-rectangle-change RECT-SELECTED "red"))
             BUDDIES))
          this))
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; rectangle-after-button-up : -> Rectangle
    ;; EFFECT: Updates this rectangle to the correct state following a button-up
    ;; DETAILS: button-up unselects all rectangle
    ;; STRATEGY: domain knowledge  (NOTE: new doesn't count)
    (define/public (rectangle-after-button-up)
      (begin 
        ;; Update this
        (set!-values (selected? x-off y-off IMG COLOR) 
                     (values false 0 0 RECT-UNSELECTED "green"))
        ;; Turn buddies green
        (for-each 
         (λ (buddy) (send buddy buddy-rectangle-change RECT-UNSELECTED "green"))
         BUDDIES)))
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; add-to-buddies : Shape% -> Void
    ;; GIVEN: a shape that overlaps this one on a drag
    ;; EFFECT: Updates the list of buddies to include the given shape
    ;; DETAILS: the shape being provided overlaps with this shape on drag,
    ;; so it is now connected and will turn red whenever the other shape is 
    ;; selected
    ;; STRATEGY: Function composition
    (define/public (add-to-buddies shape)
      (set! BUDDIES (cons shape BUDDIES)))
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; add-buddies : List<Shape%> -> Void
    ;; GIVEN: a list of Shape% currently in the World (excluding the target
    ;; EFFECT: Updates the list of buddies this rectangle has following a drag
    ;; DETAILS: if a rectangle is dragged over another rectangle, it adds it to
    ;; the list of buddies
    ;; STRATEGY: HOFC
    (define/public (add-buddies shapes)
      (for-each
       ;; GIVEN: a shape
       ;; EFFECT: updates BUDDIES of this and of shape if this overlaps with 
       ;; shape
       (λ (shape)
         (if (send this shapes-overlap? shape)
             (begin
               ;; Add to buddies for this
               (send this add-to-buddies shape)
               ;; Add this to shape's buddies
               (send shape add-to-buddies this))
             this))
       shapes))
    
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; shapes-overlap? : Shape% -> Boolean
    ;; GIVEN: a shape in the world
    ;; RETURNS: true if the shape overlaps with this one on a drag
    ;; STRATEGY: HOFC
    (define/public (shapes-overlap? shape)
      (let ([corners (send this find-corners)])
        ;; Any overlap involves at least 1 corner being inside the other shape
        (ormap
         (λ (corner) (send shape in-shape? (first corner) (second corner)))
         corners)))
        
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; find-corners : -> List<Corner>
    ;; RETURNS: a list containing four Corners, which are a list containing a
    ;; x and y value
    ;; STRATEGY: Domain Knowledge
    (define/public (find-corners)
      (list (list (- x HALF-RECT-WIDTH) (- y HALF-RECT-HEIGHT))
            (list (- x HALF-RECT-WIDTH) (+ y HALF-RECT-HEIGHT))
            (list (+ x HALF-RECT-WIDTH) (- y HALF-RECT-HEIGHT))
            (list (+ x HALF-RECT-WIDTH) (+ y HALF-RECT-HEIGHT))))
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; add-to-scene : Scene -> Scene
    ;; GIVEN: a scene
    ;; RETURNS: a scene like the given one, but with this rectangle painted
    ;; on it.
    ;; STRATEGY: function composition
    (define/public (add-to-scene scene)
      (place-image IMG x y scene))
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; in-shape? : Number Number -> Boolean
    ;; GIVEN: a location on the canvas
    ;; RETURNS: true iff the location is inside this rectangle.
    ;; STRATEGY: Domain knowledge
    (define/public (in-shape? mouse-x mouse-y)
      (and (>= mouse-x (- x HALF-RECT-WIDTH))
           (<= mouse-x (+ x HALF-RECT-WIDTH))
           (>= mouse-y (- y HALF-RECT-HEIGHT))
           (<= mouse-y (+ y HALF-RECT-HEIGHT))))
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; is-selected? : -> Boolean
    ;; RETURNS: true if rectangle is selected, else false
    ;; STRATEGY: Structural decomposition on rectangle
    (define/public (is-selected?) 
      (send this get-selected?))
    
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; limit-x: Number -> Number
    ;; GIVEN: a mouse x coordinate on a drag
    ;; RETURNS: the x half of the rectangle's length
    ;; away from border if out of bounds, else returns new-x
    ;; STRATEGY: DOMAIN KNOWLEDGE
    (define/public (limit-x new-x)
      (cond
        [(< new-x HALF-RECT-WIDTH) HALF-RECT-WIDTH]
        [(> new-x RIGHT-BOUNDARY) RIGHT-BOUNDARY]
        [else new-x]))
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; limit-y: Number -> Number
    ;; GIVEN: a mouse y coordinate on a drag
    ;; RETURNS: the y half of the rectangle's height
    ;; away from border if out of bounds, else returns new-y
    ;; STRATEGY: DOMAIN KNOWLEDGE
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
      COLOR)
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
        
    ;; getters:
    ;; get-x : -> PosInt
    ;; get-y : -> PosInt
    ;; get-x-off : -> Int
    ;; get-y-off : -> Int
    ;; get-selected? : -> Boolean
    (define/public (get-x) x)
    (define/public (get-y) y)
    (define/public (get-x-off) x-off)
    (define/public (get-y-off) y-off)
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
       (= y-off (send that get-y-off))))
    
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
    ;; STRATEGY: Domain Knowledge
    (define/public (on-tick)
      this)
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; on-key : KeyEvent -> Target
    ;; EFFECT: Does nothing, as Target% ignores on-key
    ;; DETAILS: a target ignores key events
    ;; STRATEGY: domain knowledge
    (define/public (on-key kev)
      this)      
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; generate-maybe-shape: KeyEvent List<Shape%> -> MaybeShape 
    ;;                                                (A Shape<%> or False)
    ;; GIVEN: A KeyEvent and a list of the shapes on the canvas already
    ;; RETURNS: A Shape<%> that corresponds to the given KeyEvent IFF it will
    ;;  fit
    ;; STRATEGY: Structural decomposition on kev
    ;; EXAMPLES: "r" returns a new rectangle, else returns "false"
    (define/public (generate-maybe-shape kev shapes)
      (cond
        [(and (key=? kev "r") (send this rect-will-fit?))
         (new Rectangle% [x x][y y][buddy-candidates shapes])]
        [else false]))
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; rect-will-fit?: -> Boolean
    ;; RETURNS: true if a new rectangle can be created, else false
    ;; STRATEGY: Function composition
    ;; EXAMPLES: 
    ;; if the target is located so that it is tangent with the left border,
    ;; false.
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
    ;; EFFECT: updates this Shape to its state following the given MouseEvent
    ;; STRATEGY: STRUCT DECOMP on evt : MouseEvent
    ;; EXAMPLES: 
    ;; "button-down" with coordinates inside of the target
    ;;  -- returns same target, but makes it selected
    ;;     and with the mouse distances for x and y recorded
    ;; "button-up" will unselect the rectangle
    ;; "drag" with mouse at center of target to 5 pixels below
    ;; original location 
    ;;  -- returns same target, but with +5 added to its
    ;;     y field.
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
    ;;DETAILS:  If the event is inside
    ;;the target, returns a target just like this one, except that it is
    ;;selected.  Otherwise returns the target unchanged.
    ;;STRATEGY: structural decomposition on this
    (define/public (target-after-button-down mouse-x mouse-y)
      (if (send this in-shape? mouse-x mouse-y)
          (set!-values (selected? x-off y-off) 
                       (values true (- x mouse-x) (- y mouse-y)))
          this))
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; in-shape? : Number Number -> Boolean
    ;; GIVEN: a location on the canvas
    ;; RETURNS: true iff the location is inside this target.
    ;; STRATEGY: DOMAIN KNOWLEDGE
    (define/public (in-shape? mouse-x mouse-y)
      (let ([x-diff (- x mouse-x)]
            [y-diff (- y mouse-y)])
        (<= (+ (* x-diff x-diff) (* y-diff y-diff))
            (* TARGET-RADIUS TARGET-RADIUS))))
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;;target-after-drag : Number Number -> Void
    ;;GIVEN: the location of a mouse event
    ;;EFFECT: Updates the target to its correct state following a drag to the
    ;; given location
    ;;DETAILS: if target is selected, move the target to the mouse location,
    ;;otherwise ignore.
    ;;STRATEGY: domain knowledge  (NOTE: new doesn't count)
    (define/public (target-after-drag mouse-x mouse-y)
      (if selected?
          (set!-values (x y) (values (send this limit-x (+ mouse-x x-off))
                                     (send this limit-y (+ mouse-y y-off))))
          this))
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; limit-x: Number -> Number
    ;; GIVEN: a new x position after a drag
    ;; RETURNS: the radius distance from the border of canvas
    ;; if the new x is out of bounds, else returns new-x
    (define/public (limit-x new-x)
      (cond
        [(< new-x TARGET-RADIUS) TARGET-RADIUS]
        [(> new-x RIGHT-BOUNDARY) RIGHT-BOUNDARY]
        [else new-x]))
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; limit-y: Number -> Number
    ;; GIVEN: a new y position after a drag
    ;; RETURNS: the radius distance from the border of canvas
    ;; if the new y is out of bounds, else returns new-y
    (define/public (limit-y new-y)
      (cond
        [(< new-y TARGET-RADIUS) TARGET-RADIUS]
        [(> new-y BOTTOM-BOUNDARY) BOTTOM-BOUNDARY]
        [else new-y]))
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; target-after-button-up : -> Void
    ;;EFFECT: Updates the target to its correct state following a button-up
    ;; DETAILS: button-up unselects the target
    ;; STRATEGY: domain knowledge  (NOTE: new doesn't count)
    (define/public (target-after-button-up)
      (set! selected? false))
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; add-to-scene : Scene -> Scene
    ;; RETURNS: a scene like the given one, but with the target painted on it.
    ;; STRATEGY: function composition
    (define/public (add-to-scene scene)
      (place-image IMG x y scene))
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;;is-selected? : -> Boolean
    ;;RETURNS: true if target is selected, else false
    ;;STRATEGY: Structural decomposition on target
    (define/public (is-selected?) 
      (send this get-selected?))
    
    
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    
    ;; get-color: -> String
    ;; RETURNS: either "red" or "green", depending on the color in
    ;; which this shape would be displayed if it were displayed now.
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

;; make-world : -> World%
;; RETURNS: Creates a world with no shapes
;; STRATEGY: Function composition
(define (make-world)
  (new World% 
       [target (new Target% 
                    [x (/ CANVAS-WIDTH 2)]
                    [y (/ CANVAS-HEIGHT 2)])]
       [shapes empty]))                   


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; run : PosNum -> World%
;; Given a frame rate (in seconds/tick),
;; creates and runs a world.  Returns the final state of the world.
(define (run frame-rate)
  (big-bang (make-world)
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

;;(run .25)

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

;; TEST TARGET
(define TESTTARGET-UNSELECTED 
  (new Target% [x 200][y 200][selected? false][x-off 0][y-off 0]))

(define TESTTARGET-SELECTED 
  (new Target% [x 200][y 200][selected? true][x-off 0][y-off 0]))

(define TESTTARGET-LIMIT-TOP-LEFT
  (new Target% [x 6][y 6][selected? true][x-off 0][y-off 0]))

(define TESTTARGET-LIMIT-BOT-RIGHT
  (new Target% [x 393][y 493][selected? true][x-off 0][y-off 0]))

;; TEST RECTANGLE
(define TESTRECTANGLE-UNSELECTED
  (new Rectangle% [x 100][y 100][buddy-candidates empty]
       [selected? false][x-off 0][y-off 0]))
(define TESTRECTANGLE-UNSELECTED-AFTERTICK
  (new Rectangle% [x 105][y 100][buddy-candidates empty]
       [selected? false][x-off 0][y-off 0]))  
(define TESTRECTANGLE-UNSELECTED-LEFT
  (new Rectangle% [x 100][y 100][buddy-candidates empty]
       [selected? false][x-off 0][y-off 0]))
(define TESTRECTANGLE-UNSELECTED-LEFT-AFTERTICK
  (new Rectangle% [x 95][y 100][buddy-candidates empty]
       [selected? false][x-off 0][y-off 0]))  
(define TESTRECTANGLE-SELECTED
  (new Rectangle% [x 100][y 100][buddy-candidates empty]
       [selected? true][x-off 0][y-off 0]))
(define TESTRECTANGLE-DRAG
  (new Rectangle% [x 105][y 100][buddy-candidates empty]
       [selected? true][x-off 0][y-off 0]))
(define TESTRECTANGLE-UNSELECTED-NEW
  (new Rectangle% [x 200][y 200][buddy-candidates empty]
       [selected? false][x-off 0][y-off 0]))

(define TESTRECTANGLE-LIMIT-TOP-LEFT
  (new Rectangle% [x 16][y 11][buddy-candidates empty]
       [selected? true][x-off 0][y-off 0]))

(define TESTRECTANGLE-LIMIT-BOT-RIGHT
  (new Rectangle% [x 383][y 488][buddy-candidates empty]
       [selected? true][x-off 0][y-off 0]))

(define TESTRECTANGLE-BOUNCE-RIGHT
  (new Rectangle% [x 383][y 250][buddy-candidates empty]
       [selected? false][x-off 0][y-off 0]))

(define TESTRECTANGLE-BOUNCE-LEFT
  (new Rectangle% [x 16][y 250][buddy-candidates empty]
       [selected? false][x-off 0][y-off 0]))

;; TEST WORLD
(define TESTWORLD 
  (new World% 
       [target TESTTARGET-UNSELECTED]
       [shapes (list TESTRECTANGLE-UNSELECTED)]))

(define TESTWORLD-SELECTED 
  (new World% 
       [target TESTTARGET-UNSELECTED]
       [shapes (list TESTRECTANGLE-SELECTED)]))

(define TESTWORLD-AFTERTICK 
  (new World% 
       [target TESTTARGET-UNSELECTED]
       [shapes (list TESTRECTANGLE-UNSELECTED-AFTERTICK)]))

(define TESTWORLD-BUTTONDOWNT 
  (new World% 
       [target TESTTARGET-SELECTED]
       [shapes (list TESTRECTANGLE-UNSELECTED)]))

(define TESTWORLD-BUTTONDOWNR 
  (new World% 
       [target TESTTARGET-UNSELECTED]
       [shapes (list TESTRECTANGLE-SELECTED)]))


(define TESTWORLD-AFTERDRAG
  (new World% 
       [target TESTTARGET-UNSELECTED]
       [shapes (list TESTRECTANGLE-DRAG)]))

(define TESTWORLD-ADDEDRECT
  (new World% 
       [target TESTTARGET-UNSELECTED]
       [shapes (list TESTRECTANGLE-UNSELECTED-NEW
                     TESTRECTANGLE-UNSELECTED)]))


;; True if two rectangles are similar, else false
(define (rect-similar? rect1 rect2)
  (and
   (equal? (send rect1 get-x) (send rect2 get-x))
   (equal? (send rect1 get-y) (send rect2 get-y))))


(check-equal? (rect-similar? (send TESTRECTANGLE-SELECTED on-key " ") 
                             TESTRECTANGLE-SELECTED) 
              true)

(check-equal? (send TESTWORLD get-x) 200)
(check-equal? (send TESTWORLD get-y) 200)


(check-equal? (send TESTWORLD-ADDEDRECT add-to-scene EMPTY-CANVAS)
              (place-image 
               (circle TARGET-RADIUS "outline" "red") 200 200
               (place-image 
                (rectangle RECT-WIDTH RECT-HEIGHT "outline" "green")
                100 100
                (place-image 
                 (rectangle RECT-WIDTH RECT-HEIGHT "outline" "green")
                 200 200
                 EMPTY-CANVAS))))

(check-equal? (send TESTWORLD-ADDEDRECT get-selected?) #f)
(check-equal? (send TESTWORLD-ADDEDRECT get-shapes)
              (list TESTRECTANGLE-UNSELECTED-NEW
                    TESTRECTANGLE-UNSELECTED))
(check-equal? (send TESTRECTANGLE-SELECTED on-tick) TESTRECTANGLE-SELECTED)


(check-equal? (send TESTRECTANGLE-UNSELECTED is-selected?) false)

(check-equal? (rect-similar? 
               (send TESTRECTANGLE-UNSELECTED rectangle-after-drag 20 20)
               TESTRECTANGLE-UNSELECTED)
              true)





;;================




