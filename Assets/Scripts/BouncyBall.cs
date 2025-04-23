using UnityEngine;

public class BouncyBall : MonoBehaviour
{
    //reference to the RigidBody component of the bouncy ball object
    private Rigidbody2D rigidBody2D;
    
    //line Y axis threshold below which if the ball passes this value, it reset to its position
    public float minimumY = -5.5f;
    void Start()
    {
        //get the RigidBody2D component attached to the bouncy ball object
        rigidBody2D = GetComponent<Rigidbody2D>();

    }


    void Update()
    {
        //check if the ball's Y position is below the minimum threshold
        if (transform.position.y < minimumY)
        {
            //resets the ball's position to the center of the viewport
            transform.position = Vector3.zero;

            //stop the ball's movement by setting its velocity to zero
            rigidBody2D.linearVelocity = Vector3.zero;
        }
      
    }

    private void OnCollisionEnter2D(Collision2D collision)
    {   
        //checks if the object that the ball has collided with if it has the tag 'Brick'
        if (collision.gameObject.CompareTag("Brick"))
        {   
            Debug.Log("brick hit!");
            //destroys the object that the ball has collided with
            Destroy(collision.gameObject);
        }
    }
}
