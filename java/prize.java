Set<Integer> set = Sets.newHashSet();
        for (int i = 1; i <= 80; i++) {
            set.add(i);
        }

        int count = 0, match = 0;
        while (true) {
            Uninterruptibles.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
            Random random = new Random();
            int number = random.nextInt(80);
            if (!set.contains(number)) {
                continue;
            }

            count++;
            if (count % 2 == 0) {
                System.out.println(number);
                match++;
                set.remove(number);
            }

            if (match >= 10) {
                break;
            }
        }
