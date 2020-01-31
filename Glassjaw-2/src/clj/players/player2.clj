(ns clj.players.player2)

(defn create-population [] )
(defn fitness [])

(defn genetic-algo [population fitness-fn fitness-lvl]
  (let [ reproduce (fn [[x y]]
                      (let [
                            first-half (first (split-at 4 x))
                            second-half (second  (split-at 4 y))
                            mutation-change 0.001
                            mutation-position (rand-int 8)
                            mutate (fn [ x ]
                                     (if (<= (/ (rand-int (count population))
                                                (count population))
                                               mutation-change)
                                       (assoc x mutation-position (rand-int (count population))) x))]
                        (concat (mutate first-half) (mutate second-half))))

         randomly-select (fn [x] [x (nth population (rand-int (count population)))])
         random-selections (map randomly-select population)
         next-generation (map reproduce random-selections)
         fitness-levels (map fitness-fn next-generation)]

        (if (> fitness-lvl (/ (reduce + fitness-levels) (count fitness-levels)))
          (genetic-algo next-generation fitness-fn fitness-lvl)
          next-generation))
  )
