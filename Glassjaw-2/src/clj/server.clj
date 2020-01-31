(ns clj.server
  (:require [org.httpkit.server :refer [run-server]]
            [clojure.data.json :as json]
            [clojure.core.async :as chan]
            [clj.ping-pong :as ping-pong])
  (:use [compojure.route :only [files not-found]]
        [compojure.core :only [defroutes GET POST DELETE ANY context]]
        org.httpkit.server)
  )

(def history-body (atom ""))

(defn fps-handler [req]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "Pew pew!"})

(defn general-handler [req]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    @history-body})

(defn history-handler [req]
  (reset! history-body (str @history-body (req :query-string) "\n"))
  {
     :status  200
     :headers {"Content-Type" "text/json"} ;(1)
     :body     (json/write-str {:status 200})
   }
  )

(defn player1-up [req]
  (ping-pong/player1-up)
  {
   :status  200
   :headers {"Content-Type" "text/json"} ;(1)
   :body     (json/write-str {:status 200})
   }
  )

(defn player1-down [req]
  (ping-pong/player1-down)
  {
   :status  200
   :headers {"Content-Type" "text/json"} ;(1)
   :body     (json/write-str {:status 200})
   }
  )


(defn player2-up [req]
  (ping-pong/player2-up)
  {
   :status  200
   :headers {"Content-Type" "text/json"} ;(1)
   :body     (json/write-str {:status 200})
   }
  )

(defn player2-down [req]
  (ping-pong/player2-down)
  {
   :status  200
   :headers {"Content-Type" "text/json"} ;(1)
   :body     (json/write-str {:status 200})
   }
  )


(defn start-handler [req]
  (ping-pong/execute)
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body   "started"}
  )

(defn ball-handler [req]
  {
   :status  200
   :headers {"Content-Type" "text/json"} ;(1)
   :body     (json/write-str {:status 200 :x (get @ping-pong/ball :x)  :y (get @ping-pong/ball :y) })
   }
  )

(defn p1-paddle-handler [req]
  {
   :status  200
   :headers {"Content-Type" "text/json"} ;(1)
   :body     (json/write-str {:status 200 :x (get @ping-pong/r-left :x)  :y (get @ping-pong/r-left :y) })
   }
  )
(defn p2-paddle-handler [req]
  {
   :status  200
   :headers {"Content-Type" "text/json"} ;(1)
   :body     (json/write-str {:status 200 :x (get @ping-pong/r-right :x)  :y (get @ping-pong/r-right :y) })
   }
  )
(defroutes app-routes ;(3)
           (GET "/" [] fps-handler)
           (POST "/player1-up" [] player1-up )
           (POST "/player1-down" [] player1-down)
           (POST "/player2-up" [] player2-up )
           (POST "/player2-down" [] player2-down)
           (GET "/get-ball" [] ball-handler)

           (GET "/player2-paddle" [] p2-paddle-handler)
           (GET "/player1-paddle" [] p1-paddle-handler)
           (POST "/history" [] history-handler)
           (GET "/start" [] start-handler )
           (ANY "/anything-goes" [] general-handler)
           (not-found "You Must Be New Here")) ;(4)

(defn -main [& args]
  (run-server #'app-routes {:port 8080})
  (println " started on port 8080"))

