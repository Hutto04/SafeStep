import clsx from 'clsx';
import Heading from '@theme/Heading';
import styles from './styles.module.css';

const FeatureList = [
  {
    title: 'Easy to Use',
    Svg: require('@site/static/img/undraw_mobile_app_re_catg.svg').default,
    description: (
      <>
        SmartSock is ready straight right away- simple wear the sock, connect to
        the app and monitor your foot health.
      </>
    ),
  },
  {
    title: 'Focus on What Matters',
    Svg: require('@site/static/img/undraw_grandma_re_rnv1.svg').default,
    description: (
      <>
        SmartSock alerts you to potential risks, allowing you to focus on daily
        life while it watches over your foot health.
      </>
    ),
  },
  {
    title: 'Powered by Innovation',
    Svg: require('@site/static/img/undraw_teaching_re_g7e3.svg').default,
    description: (
      <>
        SmartSock combines cutting-edge sensors and data analysis to detect
        early signs of foot ulcers, integrated seamlessly into a user-friendly
        app.
      </>
    ),
  },
];

function Feature({ Svg, title, description }) {
  return (
    <div className={clsx('col col--4')}>
      <div className="text--center">
        <Svg className={styles.featureSvg} role="img" />
      </div>
      <div className="text--center padding-horiz--md">
        <Heading as="h3">{title}</Heading>
        <p>{description}</p>
      </div>
    </div>
  );
}

export default function HomepageFeatures() {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className="row">
          {FeatureList.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  );
}
