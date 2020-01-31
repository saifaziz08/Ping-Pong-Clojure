(ns clj.players.player1
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]))

(def default-params {:content-type       :json
                     :insecure?          true
                     :socket-timeout     1000               ;; in milliseconds
                     :connection-timeout 1000               ;; in milliseconds
                     })
(defn run []
  (loop [x 2000]
    (when (> x 1)
      (let [ball (select-keys (->>
                                (client/get "http://localhost:8080/get-ball" default-params)
                                :body
                                json/read-str
                                clojure.walk/keywordize-keys
                                ) [:x :y])
            p1-pad (select-keys (->>
                                  (client/get "http://localhost:8080/player1-paddle" default-params)
                                  :body
                                  json/read-str
                                  clojure.walk/keywordize-keys
                                  ) [:y])
            p2-pad (select-keys (->>
                                  (client/get "http://localhost:8080/player2-paddle" default-params)
                                  :body
                                  json/read-str
                                  clojure.walk/keywordize-keys
                                  ) [:y])
            ]

        (if (< (get ball :y) (+ 10 (get p1-pad :y)))
          (client/post "http://localhost:8080/player1-up" default-params))
        (if-not (< (get ball :y) (+ 10 (get p1-pad :y)))
          (client/post "http://localhost:8080/player1-down" default-params))

        (if (< (get ball :y) (+ 10 (get p2-pad :y)))
          (client/post "http://localhost:8080/player2-up" default-params))
        (if-not (< (get ball :y) (+ 10 (get p2-pad :y)))
          (client/post "http://localhost:8080/player2-down" default-params))
        )
      (recur (- x 1))
      )
    )
  )