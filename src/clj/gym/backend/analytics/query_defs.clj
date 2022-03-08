(ns gym.backend.analytics.query-defs
  (:require [honey.sql :as sql]
            [honey.sql.helpers :as h]))

;; List of values to search params are provided
;; as individual key-value pairs e.g. ?tags=walk&tags=ski
(defn search-param->vector [value]
  (if (vector? value)
    value
    (if value
      [value]
      nil)))

(def workout-duration-by-tag
  {:build (fn [{:keys [user-id exclude]}]
            (let [exclude (search-param->vector exclude)]
              (-> (h/select [:tags.tag #_as :tag] [[:sum :public.workouts.duration] #_as :duration])
                  (h/from :public.workouts)
                  (h/left-join [:public.workout_tags :tags] [:= :public.workouts.workout_id :tags.workout_id])
                  (h/where (let [where [:= :public.workouts.user_id user-id]]
                             (if (empty? exclude)
                               where
                               [:and where
                                [:not-in :tag exclude]])))
                  (h/group-by :tags.tag)
                  (h/order-by [:tags.tag :asc])
                  (sql/format))))
   :params {:req [:user-id]
            :opt [:exclude]}})

(def workout-duration-this-week
  {:build (fn [{:keys [user-id]}]
            (-> (h/select [[:sum :public.workouts.duration] #_as :duration])
                (h/from :public.workouts)
                (h/where [:and
                          [:= :public.workouts.user_id user-id]
                          [[:>= :public.workouts.date [:date_trunc :'week' [:now]]]]])
                (sql/format)))
   :params {:req [:user-id]}
   :resolve
   (fn [res]
     {:duration (or (-> res first :duration) 0)})})

(def workout-duration-this-month
  {:build (fn [{:keys [user-id]}]
            (-> (h/select [[:sum :public.workouts.duration] #_as :duration])
                (h/from :public.workouts)
                (h/where [:and
                          [:= :public.workouts.user_id user-id]
                          [[:>= :public.workouts.date [:date_trunc :'month' [:now]]]]])
                (sql/format)))
   :params {:req [:user-id]}
   :resolve
   (fn [res]
     {:duration (or (-> res first :duration) 0)})})

(def workouts-by-day-of-week
  {:build (fn [{:keys [user-id]}]
            (-> (h/select [[:cast [:extract [:raw "isodow FROM public.workouts.date"]] #_as :integer] #_as :date]
                          [:tags.tag #_as :tag]
                          [:%count.* #_as :count])
                (h/from :public.workouts)
                (h/left-join [:public.workout_tags :tags] [:= :public.workouts.workout_id :tags.workout_id])
                (h/where [:= :public.workouts.user_id user-id])
                (h/group-by [:cast [:extract [:raw "isodow FROM public.workouts.date"]] #_as :integer] :tags.tag)
                (h/order-by [[:cast [:extract [:raw "isodow FROM public.workouts.date"]] #_as :integer] :asc]
                            [:tags.tag :asc])
                (sql/format)))
   :params {:req [:user-id]}
   :resolve
   (fn [res]
     {:all-tags (reduce #(conj %1 (:tag %2)) #{} res)
      :entries (->> res
                    (group-by :date)
                    (map (fn [[date maps]]
                           (reduce (fn [acc {:keys [tag count]}]
                                     (assoc acc tag count))
                                   {:date date}
                                   maps))))})})

(def workouts-by-month-of-year
  {:build (fn [{:keys [user-id]}]
            (-> (h/select [[:cast [:extract [:raw "month FROM public.workouts.date"]] #_as :integer] #_as :date]
                          [:tags.tag #_as :tag]
                          [:%count.* #_as :count])
                (h/from :public.workouts)
                (h/left-join [:public.workout_tags :tags] [:= :public.workouts.workout_id :tags.workout_id])
                (h/where [:= :public.workouts.user_id user-id])
                (h/group-by [:cast [:extract [:raw "month FROM public.workouts.date"]] #_as :integer] :tags.tag)
                (h/order-by [[:cast [:extract [:raw "month FROM public.workouts.date"]] #_as :integer] :asc]
                            [:tags.tag :asc])
                (sql/format)))
   :params {:req [:user-id]}
   :resolve
   (fn [res]
     {:all-tags (reduce #(conj %1 (:tag %2)) #{} res)
      :entries (->> res
                    (group-by :date)
                    (map (fn [[date maps]]
                           (reduce (fn [acc {:keys [tag count]}]
                                     (assoc acc tag count))
                                   {:date date}
                                   maps)))
                    (sort-by :date))})})

(def query-defs
  {:workout-duration-this-week workout-duration-this-week
   :workout-duration-this-month workout-duration-this-month
   :workout-duration-by-tag workout-duration-by-tag
   :workouts-by-day-of-week workouts-by-day-of-week
   :workouts-by-month-of-year workouts-by-month-of-year})
